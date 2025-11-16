package com.example.wallet_service_micro.dto.transferMoney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for transferring money to another user's wallet")
public class TransferRequest {

    @NotNull(message = "ReceiverId is required")
    @Schema(description = "User ID of the receiver", example = "42")
    private Long receiverId;

    @NotNull(message = "Amount is required")
    @Schema(description = "Amount of money to transfer", example = "150.00")
    private Double amount;

    @NotNull(message = "Sender wallet name is required")
    @Schema(description = "Wallet name from which the amount will be deducted", example = "main_wallet")
    private String senderWalletName;

    public TransferRequest() {}

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }
}
