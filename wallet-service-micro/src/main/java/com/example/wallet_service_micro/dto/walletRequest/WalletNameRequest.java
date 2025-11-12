package com.example.wallet_service_micro.dto.walletRequest;

public class WalletNameRequest {
    private Long userId;
    private String walletName;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWalletName() {
        return walletName;
    }
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}
