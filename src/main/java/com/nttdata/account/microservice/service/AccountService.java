package com.nttdata.account.microservice.service;

import com.nttdata.account.microservice.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<Account> save(Mono<Account> account);
    Mono<Void> deleteById(String id);
    Mono<Account> findById(String id);
    Flux<Account> findAll();
    Mono<Account> update(Mono<Account> account, String id);
    Flux<Account> findAllByCustomerId(String customerId);
}
