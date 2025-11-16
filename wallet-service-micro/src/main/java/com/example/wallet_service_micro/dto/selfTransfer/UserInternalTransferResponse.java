package com.example.wallet_service_micro.dto.selfTransfer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after an internal transfer between user's own wallets.")
public class UserInternalTransferResponse {

    @Schema(description = "Name of the sender wallet", example = "Savings")
    private String senderWalletName;

    @Schema(description = "Name of the receiver wallet", example = "Travel")
    private String receiverWalletName;

    @Schema(description = "Amount successfully transferred", example = "500.0")
    private Double amountTransferred;

    @Schema(description = "Updated balance of the sender wallet after transfer", example = "1500.75")
    private Double senderBalance;

    @Schema(description = "Updated balance of the receiver wallet after transfer", example = "2300.25")
    private Double receiverBalance;

    @Schema(
            description = "Indicates whether the sender wallet is frozen due to risk rules",
            example = "false"
    )
    private Boolean senderFrozen;

    @Schema(
            description = "Status or result message of the internal transfer",
            example = "Transfer completed successfully"
    )
    private String message;

    @Schema(
            description = "Remaining daily transfer limit of the user after this transfer",
            example = "4500.0"
    )
    private Double remainingDailyLimit;

    public Double getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(Double remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }

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
