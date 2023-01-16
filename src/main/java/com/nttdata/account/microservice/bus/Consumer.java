package com.nttdata.account.microservice.bus;

import com.nttdata.account.microservice.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Consumer {
    @Autowired
    private AccountService service;

    @KafkaListener(topics = "reversion_topic",groupId = "group_id")
    public void consumeMessage(String message) {
        log.info("Consuming Reversion with ID {}.", message);
        service.deleteById(message).subscribe();
    }
}
