package com.chatgenius.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ChatGeniusException {
    public ResourceNotFoundException(String resource, String identifier) {
        super(
            String.format("%s not found with identifier: %s", resource, identifier),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }
} 