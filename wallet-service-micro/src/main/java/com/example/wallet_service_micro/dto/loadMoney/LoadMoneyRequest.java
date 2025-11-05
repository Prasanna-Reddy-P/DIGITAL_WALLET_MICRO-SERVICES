package com.example.wallet_service_micro.dto.loadMoney;
import jakarta.validation.constraints.NotNull;

public class LoadMoneyRequest {

    @NotNull(message = "Amount is required")
    private Double amount;

    public LoadMoneyRequest() {}

    public LoadMoneyRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() { return amount; }

    public void setAmount(Double amount) { this.amount = amount; }
}
