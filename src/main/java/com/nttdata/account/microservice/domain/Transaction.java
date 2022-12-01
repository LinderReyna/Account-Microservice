package com.nttdata.account.microservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Document
public class Transaction {
    @Id
    private String id;
    private BigDecimal transactionAmount;
    private String referenceInformation;
    private OffsetDateTime processingDate;
    private String type;
    private String accountId;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
