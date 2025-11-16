package com.example.wallet_service_micro.dto.BlackList;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after un-blacklisting wallets for a user")
public class walletUnblacklistResponse {

    @Schema(description = "ID of the user", example = "101")
    private Long userId;

    @Schema(description = "Count of wallets that were un-blacklisted", example = "3")
    private int walletCount;

    @Schema(description = "Message describing the result", example = "Successfully un-blacklisted all wallets")
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
