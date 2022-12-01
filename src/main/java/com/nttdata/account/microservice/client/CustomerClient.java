package com.nttdata.account.microservice.client;

import com.nttdata.account.microservice.exception.NotFoundException;
import com.nttdata.account.microservice.model.Customer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "customer-microservice", url = "${customer.service.url}", path = "${customer.service.path}")
public interface CustomerClient {

    @GetMapping(value = "/customer/{id}")
    @CircuitBreaker(name = "CustomerCircuitBreaker", fallbackMethod = "CustomerFallback")
    Mono<ResponseEntity<Customer>> findById(@PathVariable("id") String id);

    @Slf4j
    final class LogHolder
    {}
    default Mono<ResponseEntity<Customer>> CustomerFallback(String id, Exception e) {
        LogHolder.log.error("failure Customer-Microservice: " + e.getMessage());
        throw new NotFoundException("failure Customer-Microservice: verify customer or try later");
    }
}
