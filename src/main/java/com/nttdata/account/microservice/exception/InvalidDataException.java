package com.nttdata.account.microservice.exception;

public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message){
        super(message);
    }
}
