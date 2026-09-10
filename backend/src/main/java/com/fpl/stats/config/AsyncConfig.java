package com.fpl.stats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables async execution and scheduled task support.
 * Thread pool sizes are configured in application.properties.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
