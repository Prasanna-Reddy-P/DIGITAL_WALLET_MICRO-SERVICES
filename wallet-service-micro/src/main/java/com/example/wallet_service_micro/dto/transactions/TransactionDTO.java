package com.example.wallet_service_micro.dto.transactions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Represents a wallet transaction")
public class TransactionDTO {

    @Schema(description = "Unique transaction ID", example = "1023")
    private Long id;

    @Schema(description = "Transaction amount", example = "250.75")
    private Double amount;

    @Schema(description = "Type of transaction (CREDIT, DEBIT, INTERNAL_TRANSFER)",
            example = "CREDIT")
    private String type;

    @Schema(description = "Sender wallet name (null for CREDIT)",
            example = "main_wallet")
    private String senderWalletName;

    @Schema(description = "Receiver wallet name (null for DEBIT)",
            example = "savings_wallet")
    private String receiverWalletName;

    @Schema(description = "Timestamp of the transaction",
            example = "2025-01-14T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Wallet involved in the transaction",
            example = "main_wallet")
    private String walletName;

    @Schema(description = "Email of user who initiated the transaction",
            example = "user@example.com")
    private String userEmail;

    // ✅ Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSenderWalletName() { return senderWalletName; }
    public void setSenderWalletName(String senderWalletName) { this.senderWalletName = senderWalletName; }

    public String getReceiverWalletName() { return receiverWalletName; }
    public void setReceiverWalletName(String receiverWalletName) { this.receiverWalletName = receiverWalletName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
