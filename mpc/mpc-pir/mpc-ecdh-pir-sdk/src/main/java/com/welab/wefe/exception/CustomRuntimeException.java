package com.welab.wefe.exception;

public class CustomRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 7009551133094957187L;

    public CustomRuntimeException(String message) {
        super(message);
    }
}