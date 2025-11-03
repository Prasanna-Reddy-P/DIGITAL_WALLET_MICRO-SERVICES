package com.example.wallet_service_micro.mapper;

import com.example.wallet_service_micro.dto.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.TransferResponse;
import com.example.wallet_service_micro.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "balance", source = "wallet.balance")
    @Mapping(target = "dailySpent", source = "wallet.dailySpent")
    @Mapping(target = "frozen", source = "wallet.frozen")
    @Mapping(target = "remainingDailyLimit", ignore = true)
    @Mapping(target = "message", ignore = true)
    LoadMoneyResponse toLoadMoneyResponse(Wallet wallet);

    @Mapping(target = "senderBalance", source = "wallet.balance")
    @Mapping(target = "frozen", source = "wallet.frozen")
    @Mapping(target = "recipientBalance", ignore = true)
    @Mapping(target = "amountTransferred", ignore = true)
    @Mapping(target = "message", ignore = true)
    TransferResponse toTransferResponse(Wallet wallet);
}
