package com.example.wallet_service_micro.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}

/*

This class (WebClientConfig) is just creating and registering a component (bean),
inside the Spring container — so that the entire application can use the same instance anywhere it’s needed.
 */