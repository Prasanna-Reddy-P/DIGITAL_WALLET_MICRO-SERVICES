package com.example.wallet_service_micro.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/*

This class (RestConfig) is just creating and registering a component (bean) of type RestTemplate
inside the Spring container — so that the entire application can use the same instance anywhere it’s needed.
 */