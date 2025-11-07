package com.example.wallet_service_micro.dto.transactions;
import java.time.LocalDateTime;

public class TransactionDTO {

    private Long id;
    private Double amount;

    // ✅ CREDIT, DEBIT, INTERNAL_TRANSFER
    private String type;

    // ✅ Helps user understand direction of money flow
    private String senderWalletName;
    private String receiverWalletName;

    // ✅ Timestamp
    private LocalDateTime timestamp;

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    private String walletName;


    // ✅ Optional - shows which user initiated the transaction
    private String userEmail;

    // Getters & Setters
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

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
