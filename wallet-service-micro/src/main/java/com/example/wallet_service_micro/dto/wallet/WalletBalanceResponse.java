package com.example.wallet_service_micro.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing wallet balance details, freeze status, and an informational message")
public class WalletBalanceResponse {

    @Schema(description = "Current wallet balance", example = "1500.75")
    private Double balance;

    @Schema(description = "Indicates whether the wallet is frozen", example = "false")
    private Boolean frozen;

    @Schema(description = "Informational message about the wallet status", example = "Balance fetched successfully")
    private String message;

    public WalletBalanceResponse() {}

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public Boolean getFrozen() { return frozen; }
    public void setFrozen(Boolean frozen) { this.frozen = frozen; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
