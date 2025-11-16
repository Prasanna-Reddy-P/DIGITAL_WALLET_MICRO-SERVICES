package com.example.wallet_service_micro.dto.transferMoney;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after a successful or failed wallet-to-wallet transfer")
public class TransferResponse {

    @Schema(description = "Amount that was transferred", example = "200.00")
    private Double amountTransferred;

    @Schema(description = "Updated balance of the sender after the transfer", example = "850.50")
    private Double senderBalance;

    @Schema(description = "Remaining daily transfer limit for the sender", example = "3000.00")
    private Double remainingDailyLimit;

    @Schema(description = "Indicates whether the sender's wallet is frozen", example = "false")
    private Boolean frozen;

    @Schema(description = "Status message of the transfer", example = "Transfer completed successfully")
    private String message;

    @Schema(description = "Wallet name from which money was deducted", example = "main_wallet")
    private String senderWalletName;

    @Schema(description = "Wallet name to which money was credited", example = "savings_wallet")
    private String receiverWalletName;

    // Getters & Setters
    public Double getAmountTransferred() { return amountTransferred; }
    public void setAmountTransferred(Double amountTransferred) { this.amountTransferred = amountTransferred; }

    public Double getSenderBalance() { return senderBalance; }
    public void setSenderBalance(Double senderBalance) { this.senderBalance = senderBalance; }

    public Double getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(Double remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

    public Boolean getFrozen() { return frozen; }
    public void setFrozen(Boolean frozen) { this.frozen = frozen; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }

    public String getReceiverWalletName() { return receiverWalletName; }
    public void setReceiverWalletName(String receiverWalletName) { this.receiverWalletName = receiverWalletName; }
}
