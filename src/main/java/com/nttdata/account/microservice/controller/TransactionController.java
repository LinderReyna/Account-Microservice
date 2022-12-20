package com.nttdata.account.microservice.controller;

import com.nttdata.account.microservice.api.TransactionApi;
import com.nttdata.account.microservice.model.Transaction;
import com.nttdata.account.microservice.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequestMapping("/api/microservice/1.0.0")
@RestController
@Slf4j
public class TransactionController implements TransactionApi {
    @Autowired
    private TransactionService service;
    @Override
    public Mono<ResponseEntity<Transaction>> addTransaction(String accountId, Mono<Transaction> transaction, ServerWebExchange exchange) {
        return service.save(accountId, transaction)
                .map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTransactionById(String accountId, String id, ServerWebExchange exchange) {
        return service.deleteById(accountId, id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Transaction>> findTransactionById(String accountId, String id, ServerWebExchange exchange) {
        return service.findById(accountId, id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Transaction>>> getTransactions(String accountId, ServerWebExchange exchange) {
        return Mono.just(service.findAll(accountId))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Transaction>> updateTransaction(String accountId, String id, Mono<Transaction> transaction, ServerWebExchange exchange) {
        return service.update(accountId, id, transaction)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
