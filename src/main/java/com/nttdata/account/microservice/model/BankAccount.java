package com.nttdata.account.microservice.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;

@Data
public class BankAccount {
    @Indexed(unique = true, sparse = true)
    private String name;
    private BigDecimal maintenance;
    private BigDecimal transactionFee;
    private Integer movement;
    private Integer freeTransaction;
    private Integer fixedDay;
}
