package com.example.wallet_service_micro.dto.selfTransfer;

public class UserInternalTransferResponse {

    private String senderWalletName;
    private String receiverWalletName;
    private Double amountTransferred;
    private Double senderBalance;
    private Double receiverBalance;
    private Boolean senderFrozen;
    private String message;
    private Double remainingDailyLimit;

    public Double getRemainingDailyLimit() {
        return remainingDailyLimit;
    }

    public void setRemainingDailyLimit(Double remainingDailyLimit) {
        this.remainingDailyLimit = remainingDailyLimit;
    }

    public UserInternalTransferResponse() {}

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }

    public String getReceiverWalletName() { return receiverWalletName; }
    public void setReceiverWalletName(String receiverWalletName) { this.receiverWalletName = receiverWalletName; }

    public Double getAmountTransferred() { return amountTransferred; }
    public void setAmountTransferred(Double amountTransferred) { this.amountTransferred = amountTransferred; }

    public Double getSenderBalance() { return senderBalance; }
    public void setSenderBalance(Double senderBalance) { this.senderBalance = senderBalance; }

    public Double getReceiverBalance() { return receiverBalance; }
    public void setReceiverBalance(Double receiverBalance) { this.receiverBalance = receiverBalance; }

    public Boolean getSenderFrozen() { return senderFrozen; }
    public void setSenderFrozen(Boolean senderFrozen) { this.senderFrozen = senderFrozen; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
