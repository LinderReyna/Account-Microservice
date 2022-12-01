package com.nttdata.account.microservice.service;

import com.nttdata.account.microservice.mapper.AccountMapper;
import com.nttdata.account.microservice.model.Account;
import com.nttdata.account.microservice.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository repository;
    @Autowired
    private AccountMapper mapper;

    public Mono<Account> save(Mono<Account> account) {
        return account.map(this::validation)
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toModel);
    }

    private Account validation(Account account) {
        return account;
    }

    public Mono<Void> deleteById(String id) {
        return findById(id)
                .map(mapper::toDocument)
                .flatMap(repository::delete);
    }

    public Mono<Account> findById(String id) {
        return repository.findById(id)
                .map(mapper::toModel);
    }

    public Flux<Account> findAll() {
        return repository.findAll()
                .map(mapper::toModel);
    }

    public Mono<Account> update(Mono<Account> account, String id) {
        return save(findById(id)
                .flatMap(c -> account)
                .doOnNext(e -> e.setId(id)));
    }
}