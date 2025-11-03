package com.example.wallet_service_micro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.wallet_service_micro.client")
public class WalletServiceMicroApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletServiceMicroApplication.class, args);
	}

}
