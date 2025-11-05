package com.example.wallet_service_micro.dto.transferMoney;

import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotNull(message = "ReceiverId is must")
    private Long receiverId;

    @NotNull(message = "Amount is must")
    private Double amount;

    public TransferRequest() {}

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
