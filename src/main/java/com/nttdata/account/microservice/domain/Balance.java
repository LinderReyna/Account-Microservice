package com.nttdata.account.microservice.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Balance {
    private BigDecimal balanceAmount;
    private String balanceType;
}
