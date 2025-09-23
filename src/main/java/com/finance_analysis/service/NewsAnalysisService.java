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
import com.finance_analysis.config.NewsAnalysisProperties;
import com.finance_analysis.dto.NewsAnalysisResponse;
import com.finance_analysis.exception.AnalysisException;
import com.finance_analysis.mapper.NewsAnalysisMapper;
import com.finance_analysis.model.NewsAnalysisRecord;

@Service
public class NewsAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(NewsAnalysisService.class);
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final String EMPTY_RESULT_MESSAGE = "n8n did not return a news analysis result, please retry later";

    private final NewsAnalysisMapper newsAnalysisMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final NewsAnalysisProperties properties;

    public NewsAnalysisService(NewsAnalysisMapper newsAnalysisMapper,
                               RestTemplateBuilder restTemplateBuilder,
                               ObjectMapper objectMapper,
                               NewsAnalysisProperties properties) {
        this.newsAnalysisMapper = newsAnalysisMapper;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    factory.setReadTimeout((int) Duration.ofSeconds(180).toMillis());
                    return factory;
                })
                .build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public NewsAnalysisResponse analyzeKeyword(String keyword) {
        if (properties.getN8nUrl() == null) {
            throw new AnalysisException("News analysis n8n webhook URL is not configured");
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            throw new AnalysisException("Keyword is required");
        }

        Map<String, Object> payload = buildPayload(normalizedKeyword);
        log.info("Forwarding keyword '{}' to n8n webhook {}", normalizedKeyword, properties.getN8nUrl());
        String rawResponse = invokeN8nWebhook(payload);

        JsonNode analysisNode;
        try {
            analysisNode = ensureAnalysisPresent(rawResponse);
        } catch (AnalysisException ex) {
            log.warn("n8n returned empty payload for keyword '{}'", normalizedKeyword);
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", ex.getMessage());
            analysisNode = errorNode;
            rawResponse = analysisNode.toString();
        }

        NewsAnalysisRecord record = new NewsAnalysisRecord();
        record.setKeyword(normalizedKeyword);
        record.setRawResponse(rawResponse);
        record.setRequestedAt(LocalDateTime.now());
        newsAnalysisMapper.insert(record);

        return new NewsAnalysisResponse(record.getId(), normalizedKeyword, analysisNode, record.getRequestedAt());
    }

    public List<NewsAnalysisResponse> fetchRecentHistory() {
        return newsAnalysisMapper.findRecent(DEFAULT_HISTORY_LIMIT)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String invokeN8nWebhook(Map<String, Object> payload) {
        try {
            return executePost(payload);
        } catch (RestClientResponseException ex) {
            log.warn("POST request to n8n failed with status {}: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            if (shouldFallbackToGet(ex)) {
                try {
                    return executeGet((String) payload.get("keyword"));
                } catch (RestClientException getEx) {
                    log.warn("GET request to n8n fallback failed", getEx);
                    throw new AnalysisException("n8n GET request failed: " + getEx.getMessage(), getEx);
                }
            }
            throw new AnalysisException("n8n responded with status " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            log.warn("Error communicating with n8n webhook", ex);
            throw new AnalysisException("Failed to call n8n webhook", ex);
        }
    }

    private boolean shouldFallbackToGet(RestClientResponseException ex) {
        if (ex.getStatusCode().value() == 405 || ex.getStatusCode().value() == 404) {
            return true;
        }
        String body = ex.getResponseBodyAsString();
        return body.toLowerCase().contains("not registered for post");
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
            throw new AnalysisException("n8n responded with status " + status.value());
        }
        return body == null ? "" : body;
    }

    private String executeGet(String keyword) {
        String uri = UriComponentsBuilder.fromUri(properties.getN8nUrl())
                .queryParam("keyword", keyword)
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

    private Map<String, Object> buildPayload(String keyword) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("keyword", keyword);
        return payload;
    }

    private NewsAnalysisResponse toResponse(NewsAnalysisRecord record) {
        JsonNode analysisNode;
        try {
            analysisNode = ensureAnalysisPresent(record.getRawResponse());
        } catch (AnalysisException ex) {
            log.warn("News analysis history record {} for keyword '{}' contains empty n8n payload", record.getId(), record.getKeyword());
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", ex.getMessage());
            analysisNode = errorNode;
        }
        return new NewsAnalysisResponse(record.getId(), record.getKeyword(), analysisNode, record.getRequestedAt());
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
            return analysisNode.isEmpty();
        }
        return false;
    }
}