package com.example.wallet_service_micro.dto.walletRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request for fetching transactions of a specific wallet with pagination")
public class WalletTransactionRequest {

    @Schema(description = "ID of the user", example = "12345")
    private Long userId;

    @Schema(description = "Name of the wallet", example = "Savings")
    private String walletName;

    @Schema(description = "Page number for pagination (zero-based)", example = "0")
    private int page = 0;

    @Schema(description = "Number of records per page", example = "10")
    private int size = 10;

    // Getters and setters
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

    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}
