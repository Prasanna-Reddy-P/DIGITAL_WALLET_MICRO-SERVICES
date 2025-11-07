package com.example.wallet_service_micro.dto.walletCreation;


public class CreateWalletResponse {

    private String walletName;
    private Double balance;
    private String message;

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
