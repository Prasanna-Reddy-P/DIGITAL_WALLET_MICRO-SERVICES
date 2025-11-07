package com.example.wallet_service_micro.dto.transferMoney;

public class TransferResponse {
    private Double amountTransferred;
    private Double senderBalance;
    //private Double recipientBalance;
    private Double remainingDailyLimit;
    private Boolean frozen;
    private String message;
    private String senderWalletName;   // name of wallet debited
    private String receiverWalletName;

    // Getters & Setters
    public Double getAmountTransferred() { return amountTransferred; }
    public void setAmountTransferred(Double amountTransferred) { this.amountTransferred = amountTransferred; }

    public Double getSenderBalance() { return senderBalance; }
    public void setSenderBalance(Double senderBalance) { this.senderBalance = senderBalance; }

    //public Double getRecipientBalance() { return recipientBalance; }
//    public void setRecipientBalance(Double recipientBalance) { this.recipientBalance = recipientBalance; }

    public Double getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(Double remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public Boolean getFrozen() { return frozen; }
    public void setFrozen(Boolean frozen) { this.frozen = frozen; }

//    public Double getRecipientBalance() {
//        return recipientBalance;
//    }

    public String getReceiverWalletName() {
        return receiverWalletName;
    }

    public void setReceiverWalletName(String receiverWalletName) {
        this.receiverWalletName = receiverWalletName;
    }

    public String getSenderWalletName() {
        return senderWalletName;
    }

    public void setSenderWalletName(String senderWalletName) {
        this.senderWalletName = senderWalletName;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}