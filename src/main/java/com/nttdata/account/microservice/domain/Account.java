package com.nttdata.account.microservice.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document
public class Account {
    @Id
    private String id;
    @Indexed(unique = true)
    private String accountNumber;
    @Indexed(unique = true)
    private String cci;
    private String productId;
    private List<String> titularId = new ArrayList<>();
    private List<String> signatoryId = null;
    private String status;
    private BigDecimal availableBalance;
    private BigDecimal retainedBalance;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
