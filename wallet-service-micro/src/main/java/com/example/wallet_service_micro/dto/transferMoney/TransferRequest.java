package com.example.wallet_service_micro.dto.transferMoney;

import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotNull(message = "ReceiverId is required")
    private Long receiverId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Sender wallet name is required")
    private String senderWalletName;

    public TransferRequest() {}

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }

}
