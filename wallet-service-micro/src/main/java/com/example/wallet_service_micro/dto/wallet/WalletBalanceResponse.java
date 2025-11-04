package com.example.wallet_service_micro.dto.wallet;

public class WalletBalanceResponse {
    private Double balance;
    private Boolean frozen;
    private String message;

    public WalletBalanceResponse() {}

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public Boolean getFrozen() { return frozen; }
    public void setFrozen(Boolean frozen) { this.frozen = frozen; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

