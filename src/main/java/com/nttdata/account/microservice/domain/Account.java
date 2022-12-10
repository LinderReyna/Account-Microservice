package com.nttdata.account.microservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private List<String> titularId = new ArrayList<>();
    private List<String> signatoryId = null;
    private String status;
    private List<Balance> balance;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
