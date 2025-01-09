package com.chatgenius.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ChatGeniusException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST.value());
    }
} 