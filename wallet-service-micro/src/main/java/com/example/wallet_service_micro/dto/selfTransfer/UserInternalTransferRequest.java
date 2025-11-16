package com.example.wallet_service_micro.dto.selfTransfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for transferring money between the user's own wallets.")
public class UserInternalTransferRequest {

    @Schema(
            description = "Name of the wallet from which money will be deducted",
            example = "Savings"
    )
    @NotNull(message = "Sender wallet name is required")
    private String senderWalletName;

    @Schema(
            description = "Name of the wallet to which money will be added",
            example = "Travel"
    )
    @NotNull(message = "Receiver wallet name is required")
    private String receiverWalletName;

    @Schema(
            description = "Transfer amount",
            example = "500.0"
    )
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
