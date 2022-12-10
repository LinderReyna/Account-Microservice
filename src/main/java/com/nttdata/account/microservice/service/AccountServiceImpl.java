package com.nttdata.account.microservice.service;

import com.nttdata.account.microservice.client.CustomerClient;
import com.nttdata.account.microservice.client.ProductClient;
import com.nttdata.account.microservice.exception.InvalidDataException;
import com.nttdata.account.microservice.exception.NotFoundException;
import com.nttdata.account.microservice.mapper.AccountMapper;
import com.nttdata.account.microservice.model.Account;
import com.nttdata.account.microservice.model.Customer;
import com.nttdata.account.microservice.model.Product;
import com.nttdata.account.microservice.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    public static final String CUSTOMER_PERSONAL = "personal";
    public static final String CUSTOMER_BUSINESS = "business";
    public static final String SAVINGS_ACCOUNT = "Ahorro";
    public static final String CURRENT_ACCOUNT = "Corriente";
    public static final String FIXED_TERM_ACCOUNT = "Plazo fijo";

    @Autowired
    private AccountRepository repository;
    @Autowired
    private AccountMapper mapper;
    @Autowired
    private CustomerClient customerClient;
    @Autowired
    private ProductClient productClient;

    public Mono<Account> save(Mono<Account> account) {
        return account.filterWhen(this::validTitular)
                .filterWhen(this::validSignatory)
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toModel);
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

    @Override
    public Flux<Account> findAllByTitularId(List<String> titularId) {
        return repository.findAllByTitularId(titularId)
                .map(mapper::toModel);
    }

    private Mono<Customer> getCustomer(String id) {
        return customerClient.findById(id);
    }

    private Mono<Product> getProduct(String id){
        return productClient.findById(id);
    }

    private Flux<Product> getProducts(){
        return productClient.findAllByName(Arrays.asList(SAVINGS_ACCOUNT, CURRENT_ACCOUNT, FIXED_TERM_ACCOUNT));
    }

    private Mono<Boolean> validSignatory(Account account) {
        Mono<Boolean> isValid = Mono.just(true);
        if (account.getSignatoryId() != null) {
            for (String id: account.getSignatoryId()) {
                isValid = getCustomer(id).flatMap(c -> {
                    if (!Objects.equals(c.getType(), CUSTOMER_BUSINESS)) {
                        return Mono.error(new InvalidDataException("El cliente " + id + " no es valido"));
                    }
                    return Mono.just(true);
                });
            }
        }
        return isValid;
    }

    private Mono<Boolean> validTitular(Account account) {
        AtomicBoolean isPersonal = new AtomicBoolean(false);
        Mono<Boolean> isValid = Mono.just(false);
        if (account.getTitularId() == null || account.getTitularId().isEmpty()){
            throw new InvalidDataException("Se debe ingresar un titular");
        }
        for (String id: account.getTitularId()) {
            isValid = getCustomer(id).flatMap(c -> {
                if (isPersonal.get()) {
                    return Mono.error(new InvalidDataException("Solo las cuentas bancarias empresariales pueden tener uno o más titulares"));
                } else if (Objects.equals(c.getType(), CUSTOMER_PERSONAL)) {
                    isPersonal.set(true);
                    return validPersonal(account, findAllByTitularId(Collections.singletonList(id)), getProducts());
                } else if (Objects.equals(c.getType(), CUSTOMER_BUSINESS)) {
                    return validBusiness(account);
                }
                return Mono.just(false);
            });
            isValid = isValid.map(b -> {
                if (!b)
                    throw new InvalidDataException("El cliente " + id + " no es valido");
                return b;
            });
        }
        return isValid;
    }

    private Mono<Boolean> validBusiness(Account account) {
        return getProduct(account.getProductId())
                .map(p -> Objects.equals(p.getType(), CURRENT_ACCOUNT))
                .map(v -> {
                    if (!v)
                        throw new InvalidDataException("Un cliente empresarial no puede tener una cuenta de ahorro o de plazo fijo");
                    return true;
                });
    }

    private Mono<Boolean> validPersonal(Account account, Flux<Account> accounts, Flux<Product> products) {
        return accountExists(products, accounts, SAVINGS_ACCOUNT)
                .map(b -> {
                    if (b)
                        throw new InvalidDataException("Un cliente personal solo puede tener un máximo de una cuenta de ahorro");
                    return true;
                })
                .zipWith(accountExists(products, accounts, CURRENT_ACCOUNT))
                .map(b -> {
                    if (b.getT2())
                        throw new InvalidDataException("Un cliente personal solo puede tener un máximo de una cuenta corriente");
                    return true;
                })
                .zipWith(accountExists(products, accounts, FIXED_TERM_ACCOUNT))
                .flatMap(b -> getProduct(account.getProductId())
                        .map(p -> Objects.equals(p.getType(), FIXED_TERM_ACCOUNT))
                        .map(v -> {
                            if (!v && b.getT2())
                                throw new InvalidDataException("Un cliente personal solo puede tener un máximo cuentas a plazo fijo");
                            return true;
                        }));
    }

    private Mono<Boolean> accountExists(Flux<Product> products, Flux<Account> accounts, String savingsAccount) {
        Mono<String> productId = products.filter(p -> Objects.equals(p.getBankAccount().getName(), savingsAccount))
                .map(Product::getId)
                .next();
        return accounts.zipWith(productId)
                .filter(a -> Objects.equals(a.getT1().getProductId(), a.getT2()))
                .count()
                .map(i -> i > 0);
    }
}