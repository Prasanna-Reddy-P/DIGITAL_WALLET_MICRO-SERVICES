package com.example.wallet_service_micro.dto.selfTransfer;

import jakarta.validation.constraints.NotNull;

public class UserInternalTransferRequest {

    @NotNull(message = "Sender wallet name is required")
    private String senderWalletName;

    @NotNull(message = "Receiver wallet name is required")
    private String receiverWalletName;

    @NotNull(message = "Amount is required")
    private Double amount;

    public UserInternalTransferRequest() {}

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }

    public String getReceiverWalletName() { return receiverWalletName; }
    public void setReceiverWalletName(String receiverWalletName) { this.receiverWalletName = receiverWalletName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}

