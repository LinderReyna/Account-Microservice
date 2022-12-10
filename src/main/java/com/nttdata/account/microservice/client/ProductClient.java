package com.nttdata.account.microservice.client;

import com.nttdata.account.microservice.exception.NotFoundException;
import com.nttdata.account.microservice.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@ReactiveFeignClient(name = "product-microservice", url = "${product.service.url}", path = "${product.service.path}")
public interface ProductClient {

    @GetMapping(value = "/product/{id}")
    @CircuitBreaker(name = "ProductCircuitBreaker", fallbackMethod = "ProductByIDFallback")
    Mono<Product> findById(@PathVariable("id") String id);

    @GetMapping(value = "/product/findByName")
    @CircuitBreaker(name = "ProductCircuitBreaker", fallbackMethod = "ProductByNameFallback")
    Flux<Product> findAllByName(@RequestParam(value = "name") List<String> name);

    @Slf4j
    final class LogHolder
    {}
    default Mono<Product> ProductByIDFallback(String id, Throwable e) {
        LogHolder.log.error("failure Product-Microservice: " + e.getMessage());
        throw new NotFoundException("failure Product-Microservice: verify product or try later");
    }

    default Flux<Product> ProductByNameFallback(List<String> name, Throwable e) {
        LogHolder.log.error("failure Product-Microservice: " + e.getMessage());
        throw new NotFoundException("failure Product-Microservice: verify product name or try later");
    }
}
