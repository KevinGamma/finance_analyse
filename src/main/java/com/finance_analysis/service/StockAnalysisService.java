package com.finance_analysis.service;

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
import com.finance_analysis.exception.StockAnalysisException;
import com.finance_analysis.mapper.StockAnalysisMapper;
import com.finance_analysis.model.StockAnalysisRecord;

@Service
public class StockAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(StockAnalysisService.class);
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final String EMPTY_RESULT_MESSAGE = "n8n \u672A\u8FD4\u56DE\u5206\u6790\u7ED3\u679C\uFF0C\u8BF7\u7A0D\u540E\u91CD\u8BD5";

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
                    factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
                    return factory;
                })
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public StockAnalysisResponse analyzeStockCode(String stockCode) {
        if (properties.getN8nUrl() == null) {
            throw new StockAnalysisException("n8n webhook URL is not configured");
        }

        Map<String, Object> payload = buildPayload(stockCode);
        log.info("Forwarding stock code '{}' to n8n webhook {}", stockCode, properties.getN8nUrl());
        String rawResponse = invokeN8nWebhook(payload);

        JsonNode analysisNode;
        try {
            analysisNode = ensureAnalysisPresent(rawResponse);
        } catch (StockAnalysisException ex) {
            log.warn("n8n returned empty payload for stock '{}'", stockCode);
            throw ex;
        }

        StockAnalysisRecord record = new StockAnalysisRecord();
        record.setStockCode(stockCode);
        record.setRawResponse(rawResponse);
        record.setRequestedAt(LocalDateTime.now());
        stockAnalysisMapper.insert(record);

        return new StockAnalysisResponse(record.getId(), stockCode, analysisNode, record.getRequestedAt());
    }

    public List<StockAnalysisResponse> fetchRecentHistory() {
        return stockAnalysisMapper.findRecent(DEFAULT_HISTORY_LIMIT)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private JsonNode ensureAnalysisPresent(String rawResponse) {
        JsonNode analysisNode = parseResponse(rawResponse);
        if (isEmptyAnalysis(rawResponse, analysisNode)) {
            throw new StockAnalysisException(EMPTY_RESULT_MESSAGE);
        }
        return analysisNode;
    }

    private String invokeN8nWebhook(Map<String, Object> payload) {
        try {
            return executePost(payload);
        } catch (RestClientResponseException ex) {
            log.warn("POST request to n8n failed with status {}: {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            if (ex.getRawStatusCode() == 405) {
                return executeGet((String) payload.get("stockCode"));
            }
            throw new StockAnalysisException("n8n responded with status " + ex.getRawStatusCode(), ex);
        } catch (RestClientException ex) {
            log.warn("Error communicating with n8n webhook", ex);
            throw new StockAnalysisException("Failed to call n8n webhook", ex);
        }
    }

    private String executePost(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(properties.getN8nUrl(), request, String.class);

        HttpStatusCode status = responseEntity.getStatusCode();
        String body = responseEntity.getBody();
        log.info("n8n POST response status: {}, body: {}", status.value(), body);
        if (status.isError()) {
            throw new StockAnalysisException("n8n responded with status " + status.value());
        }
        return body == null ? "" : body;
    }

    private String executeGet(String stockCode) {
        String uri = UriComponentsBuilder.fromUri(properties.getN8nUrl())
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
            throw new StockAnalysisException("n8n GET responded with status " + status.value());
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
        payload.put("value", stockCode);
        return payload;
    }

    private StockAnalysisResponse toResponse(StockAnalysisRecord record) {
        JsonNode analysisNode;
        try {
            analysisNode = ensureAnalysisPresent(record.getRawResponse());
        } catch (StockAnalysisException ex) {
            log.warn("Stock analysis history record {} for '{}' contains empty n8n payload", record.getId(), record.getStockCode());
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", ex.getMessage());
            analysisNode = errorNode;
        }
        return new StockAnalysisResponse(record.getId(), record.getStockCode(), analysisNode, record.getRequestedAt());
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
}

