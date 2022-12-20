package com.nttdata.account.microservice.service;

import com.nttdata.account.microservice.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<Transaction> save(String accountId, Mono<Transaction> transaction);

    Mono<Void> deleteById(String accountId, String id);

    Mono<Transaction> findById(String accountId, String id);

    Flux<Transaction> findAll(String accountId);

    Mono<Transaction> update(String accountId, String id, Mono<Transaction> transaction);
}
