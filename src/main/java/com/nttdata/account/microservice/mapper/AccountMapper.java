package com.nttdata.account.microservice.mapper;

import com.nttdata.account.microservice.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toModel(com.nttdata.account.microservice.domain.Account account);
    com.nttdata.account.microservice.domain.Account toDocument(Account product);
}