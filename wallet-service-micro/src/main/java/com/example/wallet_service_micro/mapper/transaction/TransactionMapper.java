package com.example.wallet_service_micro.mapper.transaction;

import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "senderWalletName", source = "senderWalletName")
    @Mapping(target = "receiverWalletName", source = "receiverWalletName")
    @Mapping(target = "walletName", source = "walletName")   // ✅ ADD THIS
    TransactionDTO toDTO(Transaction transaction);

    // ✅ Map list of Transactions to list of DTOs
    List<TransactionDTO> toDTOList(List<Transaction> transactions);
}
