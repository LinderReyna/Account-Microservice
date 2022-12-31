package com.nttdata.account.microservice.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Customer {
    private String id;
    private String type;
    private Personal personal;
    private Business business;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
