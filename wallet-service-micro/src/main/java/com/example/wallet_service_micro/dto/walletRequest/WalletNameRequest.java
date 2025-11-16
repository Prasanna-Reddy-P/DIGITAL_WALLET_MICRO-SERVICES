package com.example.wallet_service_micro.dto.walletRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request containing the wallet name and optionally user ID")
public class WalletNameRequest {

    @Schema(description = "ID of the user", example = "12345")
    private Long userId;

    @Schema(description = "Name of the wallet", example = "Savings")
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
