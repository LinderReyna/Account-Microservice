package com.nttdata.account.microservice.mapper;

import com.nttdata.account.microservice.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toModel(com.nttdata.account.microservice.domain.Transaction transaction);
    com.nttdata.account.microservice.domain.Transaction toDocument(Transaction transaction);
    Transaction copyModel(Transaction transaction);
}
