package com.fpl.stats.services.util;

import com.fpl.stats.exception.SyncException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * HTTP client for the Fantasy Premier League public API.
 *
 * <p>All outbound FPL API calls must go through this class. The base URL is configured
 * via the {@code fpl.api.base-url} property.</p>
 */
@Component
public class FplApiClient {

    private final String fplBaseUrl;
    private final RestTemplate restTemplate;

    /**
     * Constructs an {@code FplApiClient} with the configured base URL and HTTP client.
     *
     * @param fplBaseUrl   the base URL of the FPL API, sourced from {@code fpl.api.base-url}
     * @param restTemplate the Spring HTTP client used to perform requests
     */
    public FplApiClient(@Value("${fpl.api.base-url}") String fplBaseUrl,
                        RestTemplate restTemplate) {
        this.fplBaseUrl = fplBaseUrl;
        this.restTemplate = restTemplate;
    }

    /**
     * Sends a GET request to the given FPL API endpoint and returns the response as a map.
     *
     * @param endpoint the relative API endpoint (e.g. {@code "/entry/1234/history/"})
     * @return the JSON response body as a {@code Map<String, Object>}
     * @throws SyncException if the response body is null
     */
    public Map<String, Object> get(String endpoint) {
        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<>() {};
        return exchange(endpoint, type);
    }

    /**
     * Sends a GET request to the given FPL API endpoint and returns the response as a list.
     * Use this for endpoints that return a JSON array (e.g. {@code /fixtures/}).
     *
     * @param endpoint the relative API endpoint (e.g. {@code "/fixtures/"})
     * @return the JSON response body as a {@code List<Map<String, Object>>}
     * @throws SyncException if the response body is null
     */
    public List<Map<String, Object>> getList(String endpoint) {
        ParameterizedTypeReference<List<Map<String, Object>>> type = new ParameterizedTypeReference<>() {};
        return exchange(endpoint, type);
    }

    /**
     * Executes an HTTP GET against the FPL API and returns the deserialized response body.
     *
     * @param endpoint the relative API endpoint to call
     * @param type     the parameterized type reference for deserialization
     * @param <T>      the expected response body type
     * @return the non-null response body
     * @throws SyncException if the response body is null
     */
    private <T> T exchange(String endpoint, ParameterizedTypeReference<T> type) {
        ResponseEntity<T> response = restTemplate.exchange(
                fplBaseUrl + endpoint, HttpMethod.GET, null, type);
        T body = response.getBody();
        if (body == null) {
            throw new SyncException("FPL API returned empty response for endpoint: " + endpoint);
        }
        return body;
    }
}
