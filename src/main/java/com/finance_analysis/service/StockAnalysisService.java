package com.finance_analysis.service;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance_analysis.config.StockAnalysisProperties;
import com.finance_analysis.dto.StockAnalysisResponse;
import com.finance_analysis.exception.AnalysisException;
import com.finance_analysis.mapper.StockAnalysisMapper;
import com.finance_analysis.model.StockAnalysisRecord;

@Service
public class StockAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(StockAnalysisService.class);
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final String EMPTY_RESULT_MESSAGE = "n8n 未返回分析结果，请稍后重试";
    private static final String TYPE_COMPREHENSIVE = "COMPREHENSIVE";
    private static final String TYPE_STRUCTURED = "STRUCTURED";

    private final StockAnalysisMapper stockAnalysisMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StockAnalysisProperties properties;

    public StockAnalysisService(StockAnalysisMapper stockAnalysisMapper,
                                RestTemplateBuilder restTemplateBuilder,
                                ObjectMapper objectMapper,
                                StockAnalysisProperties properties) {
        this.stockAnalysisMapper = stockAnalysisMapper;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    factory.setReadTimeout((int) Duration.ofSeconds(1800).toMillis());
                    return factory;
                })
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public StockAnalysisResponse analyzeStockCode(String stockCode, String analysisType) {
        String normalizedType = normalizeType(analysisType);
        if (TYPE_STRUCTURED.equals(normalizedType)) {
            return performStructuredAnalysis(stockCode);
        }
        return performComprehensiveAnalysis(stockCode);
    }

    public List<StockAnalysisResponse> fetchRecentHistory() {
        return stockAnalysisMapper.findRecent(DEFAULT_HISTORY_LIMIT)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public String analyzeSingleStockRaw(String stockCode) {
        URI structuredUrl = properties.getStructuredUrl();
        if (structuredUrl == null) {
            throw new AnalysisException("Structured n8n webhook URL is not configured");
        }
        String uri = UriComponentsBuilder
                .fromUri(structuredUrl)
                .queryParam("stock_name", stockCode)
                .queryParam("stockCode", stockCode)
                .queryParam("code", stockCode)
                .queryParam("ticker", stockCode)
                .queryParam("symbol", stockCode)
                .queryParam("stock_code", stockCode)
                .queryParam("tickerSymbol", stockCode)
                .build(true)
                .toUriString();
        log.info("Calling single-stock analysis via GET: {}", uri);
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return resp.getBody() == null ? "" : resp.getBody();
            }
            throw new AnalysisException("Upstream responded with status " + resp.getStatusCode().value());
        } catch (RestClientException ex) {
            log.warn("Error communicating with n8n webhook", ex);
            throw new AnalysisException("Failed to call n8n webhook", ex);
        }
    }

    private StockAnalysisResponse performComprehensiveAnalysis(String stockCode) {
        URI webhookUrl = properties.getComprehensiveUrl();
        if (webhookUrl == null) {
            throw new AnalysisException("Comprehensive n8n webhook URL is not configured");
        }
        Map<String, Object> payload = buildPayload(stockCode);
        log.info("Forwarding stock code '{}' to comprehensive n8n webhook {}", stockCode, webhookUrl);
        String rawResponse = invokeN8nWebhook(webhookUrl, payload, stockCode);
        return persistAnalysis(stockCode, TYPE_COMPREHENSIVE, rawResponse);
    }

    private StockAnalysisResponse performStructuredAnalysis(String stockCode) {
        URI webhookUrl = properties.getStructuredUrl();
        if (webhookUrl == null) {
            throw new AnalysisException("Structured n8n webhook URL is not configured");
        }
        Map<String, Object> payload = buildPayload(stockCode);
        log.info("Forwarding stock code '{}' to structured n8n webhook {}", stockCode, webhookUrl);
        String rawResponse = invokeN8nWebhook(webhookUrl, payload, stockCode);
        return persistAnalysis(stockCode, TYPE_STRUCTURED, rawResponse);
    }

    private StockAnalysisResponse persistAnalysis(String stockCode, String analysisType, String rawResponse) {
        JsonNode analysisNode;
        String persistedResponse = rawResponse;
        try {
            analysisNode = ensureAnalysisPresent(rawResponse);
        } catch (AnalysisException ex) {
            log.warn("n8n returned empty payload for stock '{}'", stockCode);
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", ex.getMessage());
            analysisNode = errorNode;
            persistedResponse = errorNode.toString();
        }

        StockAnalysisRecord record = new StockAnalysisRecord();
        record.setStockCode(stockCode);
        record.setAnalysisType(analysisType);
        record.setRawResponse(persistedResponse);
        record.setRequestedAt(LocalDateTime.now());
        stockAnalysisMapper.insert(record);

        return new StockAnalysisResponse(record.getId(), stockCode, analysisType, analysisNode, record.getRequestedAt());
    }

    private String invokeN8nWebhook(URI webhookUrl, Map<String, Object> payload, String stockCode) {
        try {
            return executePost(webhookUrl, payload);
        } catch (RestClientResponseException ex) {
            log.warn("POST request to n8n failed with status {}: {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            if (shouldFallbackToGet(ex)) {
                try {
                    return executeGet(webhookUrl, stockCode);
                } catch (RestClientException getEx) {
                    log.warn("GET request to n8n fallback failed", getEx);
                    throw new AnalysisException("n8n GET request failed: " + getEx.getMessage(), getEx);
                }
            }
            throw new AnalysisException("n8n responded with status " + ex.getRawStatusCode(), ex);
        } catch (RestClientException ex) {
            log.warn("Error communicating with n8n webhook", ex);
            throw new AnalysisException("Failed to call n8n webhook", ex);
        }
    }

    private boolean shouldFallbackToGet(RestClientResponseException ex) {
        if (ex.getRawStatusCode() == 405 || ex.getRawStatusCode() == 404) {
            return true;
        }
        String body = ex.getResponseBodyAsString();
        return body != null && body.toLowerCase().contains("not registered for post");
    }

    private String executePost(URI webhookUrl, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(webhookUrl, request, String.class);

        HttpStatusCode status = responseEntity.getStatusCode();
        String body = responseEntity.getBody();
        log.info("n8n POST response status: {}, body: {}", status.value(), body);
        if (status.isError()) {
            throw new AnalysisException("n8n responded with status " + status.value());
        }
        return body == null ? "" : body;
    }

    private String executeGet(URI webhookUrl, String stockCode) {
        String uri = UriComponentsBuilder.fromUri(webhookUrl)
                .queryParam("stock_name", stockCode)
                .queryParam("stockCode", stockCode)
                .queryParam("code", stockCode)
                .queryParam("ticker", stockCode)
                .queryParam("symbol", stockCode)
                .queryParam("stock_code", stockCode)
                .queryParam("tickerSymbol", stockCode)
                .build(true)
                .toUriString();
        log.info("Retrying n8n webhook via GET: {}", uri);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        HttpStatusCode status = responseEntity.getStatusCode();
        String body = responseEntity.getBody();
        log.info("n8n GET response status: {}, body: {}", status.value(), body);
        if (status.isError()) {
            throw new AnalysisException("n8n GET responded with status " + status.value());
        }
        return body == null ? "" : body;
    }

    private Map<String, Object> buildPayload(String stockCode) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("stockCode", stockCode);
        payload.put("code", stockCode);
        payload.put("ticker", stockCode);
        payload.put("symbol", stockCode);
        payload.put("stock_code", stockCode);
        payload.put("tickerSymbol", stockCode);
        payload.put("stock_name", stockCode);
        payload.put("value", stockCode);
        return payload;
    }

    private StockAnalysisResponse toResponse(StockAnalysisRecord record) {
        JsonNode analysisNode;
        try {
            analysisNode = ensureAnalysisPresent(record.getRawResponse());
        } catch (AnalysisException ex) {
            log.warn("Stock analysis history record {} for '{}' contains empty n8n payload", record.getId(), record.getStockCode());
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", ex.getMessage());
            analysisNode = errorNode;
        }
        String type = normalizeType(record.getAnalysisType());
        return new StockAnalysisResponse(record.getId(), record.getStockCode(), type, analysisNode, record.getRequestedAt());
    }

    private JsonNode ensureAnalysisPresent(String rawResponse) {
        JsonNode analysisNode = parseResponse(rawResponse);
        if (isEmptyAnalysis(rawResponse, analysisNode)) {
            throw new AnalysisException(EMPTY_RESULT_MESSAGE);
        }
        return analysisNode;
    }

    private JsonNode parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(rawResponse);
        } catch (JsonProcessingException ex) {
            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("rawResponse", rawResponse);
            return wrapper;
        }
    }

    private boolean isEmptyAnalysis(String rawResponse, JsonNode analysisNode) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return true;
        }
        if (analysisNode == null || analysisNode.isMissingNode() || analysisNode.isNull()) {
            return true;
        }
        if (analysisNode.isTextual()) {
            return analysisNode.textValue() == null || analysisNode.textValue().trim().isEmpty();
        }
        if (analysisNode.isArray() || analysisNode.isObject()) {
            return analysisNode.size() == 0;
        }
        return false;
    }

    private String normalizeType(String analysisType) {
        if (analysisType == null || analysisType.isBlank()) {
            return TYPE_COMPREHENSIVE;
        }
        String upper = analysisType.trim().toUpperCase();
        return TYPE_STRUCTURED.equals(upper) ? TYPE_STRUCTURED : TYPE_COMPREHENSIVE;
    }
}
