package com.nttdata.account.microservice.repository;

import com.nttdata.account.microservice.domain.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Mono<Transaction> findByAccountIdAndId(String accountID, String id);
    Flux<Transaction> findAllByAccountId(String accountId);
}
