package com.example.workflow.exceptions;

public class OtpExceedingException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public OtpExceedingException(String message) {
        super(message);
    }
}