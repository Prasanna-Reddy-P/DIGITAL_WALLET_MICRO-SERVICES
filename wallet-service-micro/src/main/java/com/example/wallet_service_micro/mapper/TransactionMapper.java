package com.example.wallet_service_micro.mapper;

import com.example.wallet_service_micro.dto.TransactionDTO;
import com.example.wallet_service_micro.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "userId", target = "userEmail") // temporarily map userId if userEmail isn't available
    TransactionDTO toDTO(Transaction transaction);
}
