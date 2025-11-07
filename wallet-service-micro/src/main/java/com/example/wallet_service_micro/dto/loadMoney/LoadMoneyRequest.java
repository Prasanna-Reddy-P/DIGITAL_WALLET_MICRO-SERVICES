package com.example.wallet_service_micro.dto.loadMoney;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;


public class LoadMoneyRequest {

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotBlank(message = "Wallet name/type is required")
    private String walletName;

    // ✅ Getters and Setters
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}
