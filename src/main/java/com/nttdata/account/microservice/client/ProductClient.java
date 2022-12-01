package com.nttdata.account.microservice.client;

import com.nttdata.account.microservice.exception.NotFoundException;
import com.nttdata.account.microservice.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "product-microservice", url = "${product.service.url}", path = "${product.service.path}")
public interface ProductClient {

    @GetMapping(value = "/product/{id}")
    @CircuitBreaker(name = "ProductCircuitBreaker", fallbackMethod = "ProductFallback")
    Mono<ResponseEntity<Product>> findById(@PathVariable("id") String id);

    @Slf4j
    final class LogHolder
    {}
    default Mono<ResponseEntity<Product>> ProductFallback(String id, Exception e) {
        LogHolder.log.error("failure Product-Microservice: " + e.getMessage());
        throw new NotFoundException("failure Product-Microservice: verify product or try later");
    }
}
