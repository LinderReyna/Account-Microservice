package com.nttdata.account.microservice.seed;

import com.nttdata.account.microservice.domain.Account;
import com.nttdata.account.microservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

@Component
public class AccountSeed {
    @Autowired
    private AccountRepository repository;

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        var titular = new ArrayList<String>();
        titular.add("63859d33434879511128cf355");
        var account = Account.builder()
                .id("63859d33434879511128cf355")
                .accountNumber("193-1315150-0-35")
                .cci("002-193-00131515003512")
                .productId("63859d33434879511128cf356")
                .titularId(titular)
                .status("ACTIVA")
                .availableBalance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();
        try {
            repository.findById(account.getId())
                    .switchIfEmpty(repository.save(account)).subscribe();
        } catch (Exception ignored) { }
    }
}
