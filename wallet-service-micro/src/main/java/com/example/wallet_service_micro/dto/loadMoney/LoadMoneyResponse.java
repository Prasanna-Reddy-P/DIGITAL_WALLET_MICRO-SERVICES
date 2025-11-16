package com.example.wallet_service_micro.dto.loadMoney;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successfully loading money into a wallet")
public class LoadMoneyResponse {

    @Schema(description = "Updated wallet balance after loading money", example = "1500.0")
    private Double balance;

    @Schema(description = "Amount spent today by the user", example = "200.0")
    private Double dailySpent;

    @Schema(description = "Remaining daily spending limit", example = "800.0")
    private Double remainingDailyLimit;

    @Schema(description = "Indicates if the wallet is currently frozen", example = "false")
    private Boolean frozen;

    @Schema(description = "Status message or description of the operation", example = "Money loaded successfully")
    private String message;

    @Schema(description = "Name of the wallet involved in the transaction", example = "Savings")
    private String walletName;

    // Getters & Setters
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance   = balance; }

    public Double getDailySpent() { return dailySpent; }
    public void setDailySpent(Double dailySpent) { this.dailySpent = dailySpent; }

    public Double getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(Double remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public Boolean getFrozen() { return frozen; }
    public void setFrozen(Boolean frozen) { this.frozen = frozen; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
}
