package com.example.wallet_service_micro.dto.walletCreation;


import jakarta.validation.constraints.NotBlank;

public class CreateWalletRequest {

    @NotBlank(message = "Wallet name is required")
    private String walletName;

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
}
