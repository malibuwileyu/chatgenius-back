package com.chatgenius.dto.response;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public abstract class BaseResponse {
    private static final Logger logger = LoggerFactory.getLogger(BaseResponse.class);
    
    private UUID id;
    private ZonedDateTime createdAt;

    protected void setId(UUID id) {
        if (id == null) {
            logger.error("Cannot set null ID in BaseResponse");
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }

    protected void setCreatedAt(ZonedDateTime createdAt) {
        if (createdAt == null) {
            logger.error("Cannot set null createdAt in BaseResponse");
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        this.createdAt = createdAt;
    }
} 