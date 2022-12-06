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
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;

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
        return account.filterWhen(this::validation)
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
    public Flux<Account> findAllByCustomerId(String customerId) {
        return repository.findAllByCustomerId(customerId)
                .map(mapper::toModel);
    }

    private Mono<Customer> getCustomer(String id) {
        return customerClient.findById(id)
                .mapNotNull(HttpEntity::getBody)
                .onErrorMap(throwable -> new NotFoundException("Cliente no encontrado, verificar ID de Cliente"));
    }

    private Mono<Product> getProduct(String id){
        return productClient.findById(id)
                .mapNotNull(HttpEntity::getBody)
                .onErrorMap(throwable -> new NotFoundException("Producto no encontrado, verificar ID de Producto"));
    }

    private Flux<Product> getProducts(){
        return productClient.findAllByName(Arrays.asList(SAVINGS_ACCOUNT, CURRENT_ACCOUNT, FIXED_TERM_ACCOUNT))
                .mapNotNull(HttpEntity::getBody)
                .flatMapMany(p -> p)
                .onErrorMap(throwable -> new NotFoundException("Producto no encontrado, verificar Nombre de Producto"));
    }

    private Mono<Boolean> validation(Account account) {
        Flux<Account> accounts = findAllByCustomerId(account.getCustomerId());
        Flux<Product> products = getProducts();
        Mono<Customer> customer = getCustomer(account.getCustomerId());

        return customer.flatMap(c -> {
            if (Objects.equals(c.getType(), CUSTOMER_PERSONAL)) {
                return validPersonal(account, accounts, products);
            } else if (Objects.equals(c.getType(), CUSTOMER_BUSINESS)) {
                return Mono.just(false);
            }
            return Mono.just(false);
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