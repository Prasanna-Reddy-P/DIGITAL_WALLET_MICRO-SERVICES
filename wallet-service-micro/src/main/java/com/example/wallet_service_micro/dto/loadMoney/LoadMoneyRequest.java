package com.example.wallet_service_micro.dto.loadMoney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;


@Schema(description = "Request payload for loading money into a specific wallet")
public class LoadMoneyRequest {


    @Schema(description = "Amount to be added to the wallet", example = "500.0", required = true)
    @NotNull(message = "Amount is required")
    private Double amount;

    @Schema(description = "Name of the wallet where money will be loaded", example = "Savings", required = true)
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
