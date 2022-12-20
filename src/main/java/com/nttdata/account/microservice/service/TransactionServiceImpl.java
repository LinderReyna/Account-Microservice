package com.nttdata.account.microservice.service;

import com.nttdata.account.microservice.exception.InvalidDataException;
import com.nttdata.account.microservice.mapper.TransactionMapper;
import com.nttdata.account.microservice.model.Account;
import com.nttdata.account.microservice.model.Balance;
import com.nttdata.account.microservice.model.BankAccount;
import com.nttdata.account.microservice.model.Transaction;
import com.nttdata.account.microservice.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    public static final String SAME_BANK_ACCOUNT = "63859d33434879511128cf355";
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private TransactionMapper mapper;
    @Autowired
    private AccountService accountService;

    @Override
    public Mono<Transaction> save(String accountId, Mono<Transaction> transaction) {
        return accountService.findById(accountId)
                .zipWith(transaction)
                .map(this::validation)
                .flatMap(t -> accountService.save(Mono.just(t.getT1())).then(Mono.just(t)))
                .flatMap(t -> toFee(t).then(Mono.just(t)))
                .flatMap(t -> toDestiny(t.getT2()).then(Mono.just(t.getT2())))
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toModel);
    }

    @Override
    public Mono<Void> deleteById(String accountId, String id) {
        return findById(accountId, id)
                .map(mapper::toDocument)
                .flatMap(repository::delete);
    }

    @Override
    public Mono<Transaction> findById(String accountId, String id) {
        return repository.findByAccountIdAndId(accountId, id)
                .map(mapper::toModel);
    }

    @Override
    public Flux<Transaction> findAll(String accountId) {
        return repository.findAllByAccountId(accountId)
                .map(mapper::toModel);
    }

    @Override
    public Mono<Transaction> update(String accountId, String id, Mono<Transaction> transaction) {
        return save(accountId, findById(accountId, id)
                .flatMap(t -> transaction)
                .doOnNext(e -> e.setId(id)));
    }

    private Mono<Void> toDestiny(Transaction transaction) {
        Transaction tran = new Transaction();
        tran.setAccountId(transaction.getDestinationAccountId());
        tran.setType(Transaction.TypeEnum.ABONO);
        tran.setDestinationAccountId(transaction.getAccountId());
        tran.setTransactionAmount(transaction.getTransactionAmount());
        tran.setDescription(transaction.getDescription());
        tran.setProcessingDate(Instant.now());
        return accountService.findById(transaction.getDestinationAccountId())
                .onErrorMap(x -> {
                    throw new InvalidDataException("Cuenta de destino no encontrada");
                })
                .zipWith(Mono.just(tran))
                .map(this::validation)
                .flatMap(t -> accountService.save(Mono.just(t.getT1())).then(Mono.just(t.getT2())))
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .then(Mono.empty());
    }

    private Mono<Void> toFee(Tuple2<Account, Transaction> t) {
        Transaction transaction = mapper.copyModel(t.getT2());
        return findAll(t.getT1().getId())
                .filter(x -> !x.getDescription().equals("Comision"))
                .count()
                .flatMap(x -> {
                    BankAccount bank = t.getT1().getProduct().getBankAccount();
                    if (bank.getFreeTransaction() < x) {
                        List<Balance> finalBalances = new ArrayList<>();
                        transaction.setTransactionAmount(transaction.getTransactionAmount().add(bank.getTransactionFee()));
                        validBalance(t.getT1().getBalance(), transaction, finalBalances);
                    }
                    transaction.setTransactionAmount(bank.getTransactionFee());
                    transaction.destinationAccountId(SAME_BANK_ACCOUNT);
                    transaction.setType(Transaction.TypeEnum.CARGO);
                    transaction.setDescription("Comision");
                    return repository.save(mapper.toDocument(transaction)).then(Mono.empty());
                });
    }

    private Tuple2<Account, Transaction> validation(Tuple2<Account, Transaction> t) {
        List<Balance> balances = t.getT1().getBalance();
        Transaction transaction = t.getT2();
        List<Balance> finalBalances = new ArrayList<>();
        validBalance(balances, transaction, finalBalances);
        transaction.setAccountId(t.getT1().getId());
        t.mapT1(r -> {
            r.setBalance(finalBalances);
            return r;
        }).mapT2(r -> transaction);
        return t;
    }

    private static void validBalance(List<Balance> balances, Transaction transaction, List<Balance> finalBalances) {
        if (balances != null) {
            if (transaction.getType().equals(Transaction.TypeEnum.CARGO) &&
                    balances.stream().filter(b -> b.getBalanceType().equals(Balance.BalanceTypeEnum.DISPONIBLE))
                            .anyMatch(b -> b.getBalanceAmount().compareTo(transaction.getTransactionAmount()) < 0))
                throw new InvalidDataException("Saldo disponible insuficiente");
            for (Balance b: balances) {
                if (transaction.getType().equals(Transaction.TypeEnum.CARGO) && b.getBalanceType().equals(Balance.BalanceTypeEnum.DISPONIBLE))
                    b.setBalanceAmount(b.getBalanceAmount().subtract(transaction.getTransactionAmount()));
                else if (transaction.getType().equals(Transaction.TypeEnum.ABONO) && b.getBalanceType().equals(Balance.BalanceTypeEnum.DISPONIBLE))
                    b.setBalanceAmount(b.getBalanceAmount().add(transaction.getTransactionAmount()));
                finalBalances.add(b);
            }
        }
    }
}
