package com.nttdata.account.microservice.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document
public class Transaction {
    @Id
    private String id;
    private BigDecimal transactionAmount;
    private String destinationAccountId;
    private Instant processingDate;
    private String type;
    private String accountId;
    private String description;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
