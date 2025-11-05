package com.example.wallet_service_micro.mapper.transaction;

import com.example.wallet_service_micro.dto.transactions.TransactionDTO;
import com.example.wallet_service_micro.model.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDTO toDTO(Transaction transaction);
}
