package com.example.wallet_service_micro.dto.BlackList;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing wallet and user blacklist status")
public class WalletBlacklistResponse {

    @Schema(description = "Unique ID of the user", example = "101")
    private Long userId;

    @Schema(description = "Name of the user's wallet", example = "MainWallet")
    private String walletName;

    @Schema(description = "Indicates whether the wallet is blacklisted", example = "true")
    private Boolean walletBlacklisted;

    @Schema(description = "Indicates whether the user is blacklisted", example = "false")
    private Boolean userBlacklisted;

    @Schema(description = "Additional message related to blacklist status", example = "Wallet has been successfully blacklisted")
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
