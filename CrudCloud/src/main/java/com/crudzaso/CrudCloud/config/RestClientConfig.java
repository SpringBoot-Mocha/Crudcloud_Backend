package com.crudzaso.CrudCloud.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST client beans used throughout the application.
 * Configures RestTemplate for external API calls (Google, GitHub).
 */
@Configuration
public class RestClientConfig {

    /**
     * Configure RestTemplate with appropriate timeouts for OAuth API calls.
     *
     * @param builder RestTemplateBuilder for configuration
     * @return configured RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
