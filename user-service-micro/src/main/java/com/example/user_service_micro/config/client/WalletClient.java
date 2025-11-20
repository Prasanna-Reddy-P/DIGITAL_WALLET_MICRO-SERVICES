package com.example.user_service_micro.config.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WalletClient {

    private final RestClient restClient = RestClient.create();

    public void createWallet(Long userId) {
        try {
            restClient.post()
                    .uri("http://localhost:8086/api/wallet/create?userId=" + userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("⚠ Wallet-service unreachable. Wallet will be created later.");
        }
    }
}

