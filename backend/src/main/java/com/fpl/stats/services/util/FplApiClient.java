package com.fpl.stats.services.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FplApiClient {
    private static final String FPL_BASE_URL = "https://fantasy.premierleague.com/api";
    private final RestTemplate restTemplate;

    public FplApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends a GET request to the specified endpoint of the Fantasy Premier League (FPL) API.
     * <p>
     * This method builds the full URL by appending the given endpoint to the base FPL API URL
     * and uses Spring's {@link RestTemplate} to perform the HTTP GET request. The response body
     * is returned as a {@code Map<String, Object>} representing the deserialized JSON structure.
     *
     * @param endpoint the relative API endpoint to call (e.g., {@code "/entry/1234/history/"}).
     * @return a map containing the JSON response from the FPL API as key-value pairs.
     */
    public Map<String, Object> get(String endpoint) {
        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<>() {};
        return restTemplate.exchange(FPL_BASE_URL + endpoint, HttpMethod.GET, null, type).getBody();
    }
}