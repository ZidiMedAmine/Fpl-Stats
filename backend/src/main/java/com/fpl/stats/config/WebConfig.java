package com.fpl.stats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://fpl-stats-frontend.onrender.com",
                        "http://localhost:4200"
                )
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}