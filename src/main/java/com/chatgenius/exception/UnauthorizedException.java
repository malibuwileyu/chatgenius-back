package com.chatgenius.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ChatGeniusException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED.value());
    }
} 