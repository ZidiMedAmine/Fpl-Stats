package com.fpl.stats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for the {@link RestTemplate} HTTP client bean.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates the application-wide {@link RestTemplate} used for all outbound HTTP calls.
     *
     * @return a default {@link RestTemplate} instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
