package com.task.spribetask.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate currencyRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${currency.service.url}") String url) {
        return restTemplateBuilder
                .rootUri(url)
                .build();
    }

}
