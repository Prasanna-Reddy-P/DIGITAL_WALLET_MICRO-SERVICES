package com.example.wallet_service_micro.dto.walletCreation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new wallet for the user")
public class CreateWalletRequest {

    @Schema(description = "Name/type of the wallet to be created", example = "Savings")
    @NotBlank(message = "Wallet name is required")
    private String walletName;

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
}
