package com.nttdata.account.microservice.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
public class Personal {
    private String names;
    private String fatherLastName;
    private String motherLastName;
    private String documentType;
    private String profile;
    private String documentNumber;
}
