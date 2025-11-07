package com.example.wallet_service_micro.dto.loadMoney;

public class LoadMoneyResponse {
    private Double balance;
    private Double dailySpent;
    private Double remainingDailyLimit;
    private Boolean frozen;
    private String message;
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

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}