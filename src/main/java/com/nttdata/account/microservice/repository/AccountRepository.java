package com.nttdata.account.microservice.repository;

import com.nttdata.account.microservice.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
    Flux<Account> findAllByTitularId(List<String> titularId);
}