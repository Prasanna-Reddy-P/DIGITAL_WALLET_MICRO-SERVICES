package com.example.wallet_service_micro.dto;

import java.time.LocalDateTime;

public class UserTransactionRangeRequest {

    private Long userId;
    private String walletName;
    private LocalDateTime start;
    private LocalDateTime end;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}
