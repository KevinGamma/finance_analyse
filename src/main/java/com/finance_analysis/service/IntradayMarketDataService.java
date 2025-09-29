package com.finance_analysis.service;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_analysis.config.StockAnalysisProperties;
import com.finance_analysis.dto.IntradayCandle;
import com.finance_analysis.dto.IntradayTimeSeriesResponse;
import com.finance_analysis.exception.AnalysisException;

@Service
public class IntradayMarketDataService {

    private static final Logger log = LoggerFactory.getLogger(IntradayMarketDataService.class);
    private static final Set<String> SUPPORTED_INTERVALS = Set.of("1min", "5min", "15min", "30min", "60min");
    private static final DateTimeFormatter ALPHA_VANTAGE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StockAnalysisProperties properties;

    public IntradayMarketDataService(RestTemplateBuilder restTemplateBuilder,
                                     ObjectMapper objectMapper,
                                     StockAnalysisProperties properties) {
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(5000);
                    factory.setReadTimeout(15000);
                    return factory;
                })
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public IntradayTimeSeriesResponse fetchIntradaySeries(String symbol, String interval) {
        String sanitizedSymbol = sanitizeSymbol(symbol);
        String validatedInterval = validateInterval(interval);
        URI requestUri = buildRequestUri(sanitizedSymbol, validatedInterval);
        log.info("Fetching intraday data for {} at interval {}", sanitizedSymbol, validatedInterval);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUri, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AnalysisException("Alpha Vantage responded with status " + response.getStatusCode().value());
            }
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new AnalysisException("Alpha Vantage returned empty response");
            }
            JsonNode root = parseBody(body);
            handleErrorPayload(root);
            return mapResponse(root, sanitizedSymbol, validatedInterval);
        } catch (RestClientException ex) {
            log.warn("Alpha Vantage request failed", ex);
            throw new AnalysisException("Failed to call Alpha Vantage API", ex);
        }
    }

    private URI buildRequestUri(String symbol, String interval) {
        URI baseUri = properties.getAlphaVantageUrl();
        if (baseUri == null) {
            throw new AnalysisException("Alpha Vantage base URL is not configured");
        }
        String apiKey = properties.getAlphaVantageApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new AnalysisException("Alpha Vantage API key is not configured");
        }
        String outputSize = properties.getIntradayOutputSize();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri)
                .queryParam("function", "TIME_SERIES_INTRADAY")
                .queryParam("symbol", symbol)
                .queryParam("interval", interval)
                .queryParam("apikey", apiKey);
        if (outputSize != null && !outputSize.isBlank()) {
            builder.queryParam("outputsize", outputSize);
        }
        return builder.build(true).toUri();
    }

    private JsonNode parseBody(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException ex) {
            throw new AnalysisException("Unable to parse Alpha Vantage response", ex);
        }
    }

    private void handleErrorPayload(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            throw new AnalysisException("Alpha Vantage returned no data");
        }
        JsonNode note = root.get("Note");
        if (note != null && !note.isNull()) {
            throw new AnalysisException(note.asText());
        }
        JsonNode error = root.get("Error Message");
        if (error != null && !error.isNull()) {
            throw new AnalysisException(error.asText());
        }
    }

    private IntradayTimeSeriesResponse mapResponse(JsonNode root, String symbol, String interval) {
        String seriesKey = "Time Series (" + interval + ")";
        JsonNode meta = root.path("Meta Data");
        JsonNode series = root.path(seriesKey);
        if (series.isMissingNode() || series.isNull() || !series.fields().hasNext()) {
            throw new AnalysisException("Alpha Vantage did not return intraday series data");
        }
        IntradayTimeSeriesResponse response = new IntradayTimeSeriesResponse();
        response.setSymbol(symbol);
        response.setInterval(interval);
        response.setLastRefreshed(meta.path("3. Last Refreshed").asText(null));
        response.setTimezone(meta.path("6. Time Zone").asText(null));

        List<IntradayCandle> candles = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = series.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String timestamp = entry.getKey();
            JsonNode values = entry.getValue();
            IntradayCandle candle = new IntradayCandle();
            candle.setTimestamp(toIsoTimestamp(timestamp, response.getTimezone()));
            candle.setOpen(parseDecimal(values.path("1. open")));
            candle.setHigh(parseDecimal(values.path("2. high")));
            candle.setLow(parseDecimal(values.path("3. low")));
            candle.setClose(parseDecimal(values.path("4. close")));
            candle.setVolume(parseVolume(values.path("5. volume")));
            candles.add(candle);
        }
        candles.sort(Comparator.comparing(IntradayCandle::getTimestamp));
        response.setCandles(candles);
        return response;
    }

    private BigDecimal parseDecimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return BigDecimal.ZERO;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse decimal value '{}'", text, ex);
            return BigDecimal.ZERO;
        }
    }

    private Long parseVolume(JsonNode node) {
        if (node == null || node.isNull()) {
            return 0L;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return 0L;
        }
        try {
            return new BigDecimal(text).longValue();
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse volume value '{}'", text, ex);
            return 0L;
        }
    }

    private String toIsoTimestamp(String timestamp, String timezone) {
        if (timestamp == null || timestamp.isBlank()) {
            return timestamp;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp, ALPHA_VANTAGE_TIMESTAMP);
            if (timezone == null || timezone.isBlank()) {
                return localDateTime.toString();
            }
            return localDateTime.atZone(java.time.ZoneId.of(timezone)).toOffsetDateTime().toString();
        } catch (DateTimeParseException ex) {
            log.debug("Unable to parse timestamp '{}'", timestamp, ex);
            return timestamp;
        } catch (Exception ex) {
            log.debug("Unable to adjust timestamp '{}' with timezone '{}'", timestamp, timezone, ex);
            return timestamp;
        }
    }

    private String sanitizeSymbol(String symbol) {
        if (symbol == null) {
            throw new AnalysisException("Stock symbol is required");
        }
        String trimmed = symbol.trim();
        if (trimmed.isEmpty()) {
            throw new AnalysisException("Stock symbol is required");
        }
        return trimmed.toUpperCase(Locale.ENGLISH);
    }

    private String validateInterval(String interval) {
        if (interval == null) {
            throw new AnalysisException("Interval is required");
        }
        String normalized = interval.trim().toLowerCase(Locale.ENGLISH);
        if (!SUPPORTED_INTERVALS.contains(normalized)) {
            throw new AnalysisException("Unsupported interval: " + interval);
        }
        return normalized;
    }
}