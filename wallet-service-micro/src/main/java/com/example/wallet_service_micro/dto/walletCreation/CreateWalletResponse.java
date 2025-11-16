package com.example.wallet_service_micro.dto.walletCreation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successfully creating a new wallet")
public class CreateWalletResponse {

    @Schema(description = "Name/type of the created wallet", example = "Savings")
    private String walletName;

    @Schema(description = "Initial balance of the wallet", example = "0.0")
    private Double balance;

    @Schema(description = "Status or success message", example = "Wallet created successfully")
    private String message;

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
