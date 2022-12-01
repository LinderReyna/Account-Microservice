package com.nttdata.account.microservice.controller;

import com.nttdata.account.microservice.api.AccountApi;
import com.nttdata.account.microservice.model.Account;
import com.nttdata.account.microservice.service.AccountService;
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
public class AccountController implements AccountApi {
    @Autowired
    private AccountService service;

    @Override
    public Mono<ResponseEntity<Account>> addAccount(Mono<Account> account, ServerWebExchange exchange) {
        return service.save(account)
                .map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccountById(String id, ServerWebExchange exchange) {
        return service.deleteById(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Account>> findAccountById(String id, ServerWebExchange exchange) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Account>>> getAccounts(ServerWebExchange exchange) {
        return Mono.just(service.findAll())
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Account>> updateAccount(String id, Mono<Account> account, ServerWebExchange exchange) {
        return service.update(account, id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}