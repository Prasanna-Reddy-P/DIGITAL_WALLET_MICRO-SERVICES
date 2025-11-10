package com.example.wallet_service_micro.dto.BlackList;

public class WalletBlacklistResponse {

    private Long userId;
    private String walletName;
    private Boolean walletBlacklisted;
    private Boolean userBlacklisted;
    private String message;

    public WalletBlacklistResponse(Long userId, String walletName, Boolean walletBlacklisted,
                                   Boolean userBlacklisted, String message) {
        this.userId = userId;
        this.walletName = walletName;
        this.walletBlacklisted = walletBlacklisted;
        this.userBlacklisted = userBlacklisted;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getWalletName() {
        return walletName;
    }

    public Boolean getWalletBlacklisted() {
        return walletBlacklisted;
    }

    public Boolean getUserBlacklisted() {
        return userBlacklisted;
    }

    public String getMessage() {
        return message;
    }
}
