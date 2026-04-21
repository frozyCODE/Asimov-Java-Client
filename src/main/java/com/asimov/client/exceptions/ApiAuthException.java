package com.asimov.client.exceptions;

public class ApiAuthException extends RuntimeException {
    public ApiAuthException(String message) {
        super(message);
    }
}