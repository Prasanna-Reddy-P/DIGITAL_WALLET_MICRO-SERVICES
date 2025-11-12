package com.example.wallet_service_micro.dto.BlackList;

public class walletUnblacklistResponse {
    private Long userId;
    private int walletCount;
    private String message;

    // ✅ No-args constructor (needed by Jackson)
    public walletUnblacklistResponse() {
    }

    // ✅ All-args constructor (for easy creation)
    public walletUnblacklistResponse(Long userId, int walletCount, String message) {
        this.userId = userId;
        this.walletCount = walletCount;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getWalletCount() {
        return walletCount;
    }

    public void setWalletCount(int walletCount) {
        this.walletCount = walletCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
