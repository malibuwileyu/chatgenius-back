package com.chatgenius.dto.response;

public class SimpleMessageResponse {
    private String message;

    public SimpleMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 