package com.example.wallet_service_micro.mapper.wallet;

import com.example.wallet_service_micro.dto.loadMoney.LoadMoneyResponse;
import com.example.wallet_service_micro.dto.selfTransfer.UserInternalTransferResponse;
import com.example.wallet_service_micro.dto.transferMoney.TransferResponse;
import com.example.wallet_service_micro.dto.wallet.WalletBalanceResponse;
import com.example.wallet_service_micro.model.wallet.Wallet;
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
    //@Mapping(target = "recipientBalance", ignore = true)
    @Mapping(target = "amountTransferred", ignore = true)
    @Mapping(target = "message", ignore = true)
    TransferResponse toTransferResponse(Wallet wallet);


    @Mapping(target = "message", ignore = true)
    @Mapping(target = "balance", source = "wallet.balance")
    @Mapping(target = "frozen", source = "wallet.frozen")
    WalletBalanceResponse toBalanceResponse(Wallet wallet);

    // ---------------------------
// map Wallet -> UserInternalTransferResponse
// ---------------------------
    @Mapping(target = "senderWalletName", source = "wallet.walletName")
    @Mapping(target = "senderBalance", source = "wallet.balance")
    @Mapping(target = "senderFrozen", source = "wallet.frozen")
    @Mapping(target = "receiverWalletName", ignore = true)
    @Mapping(target = "receiverBalance", ignore = true)
    @Mapping(target = "amountTransferred", ignore = true)
    @Mapping(target = "remainingDailyLimit", ignore = true)
    @Mapping(target = "message", ignore = true)
    UserInternalTransferResponse toInternalTransferResponse(Wallet wallet);

}
