package com.nttdata.account.microservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Document
public class Account {
    @Id
    private String id;
    @Indexed(unique = true)
    private String accountNumber;
    @Indexed(unique = true)
    private String cci;
    private String productId;
    private String customerId;
    private String status;
    private List<Balance> balance;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
