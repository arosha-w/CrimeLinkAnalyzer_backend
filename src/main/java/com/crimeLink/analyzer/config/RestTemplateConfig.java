package com.crimeLink.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate bean used to communicate with ML microservices.
 * Part of the hybrid monolith + microservices architecture.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Set timeout for ML service calls (30 seconds for heavy processing)
        factory.setConnectTimeout(10000);  // 10 seconds connection timeout
        factory.setReadTimeout(30000);     // 30 seconds read timeout (ML processing can be slow)
        return new RestTemplate(factory);
    }
}
