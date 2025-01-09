package com.chatgenius.dto;

import com.chatgenius.model.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateChannelRequest {
    @NotBlank(message = "Channel name is required")
    @Size(min = 3, max = 50, message = "Channel name must be between 3 and 50 characters")
    private String name;

    @NotNull(message = "Channel type is required")
    private ChannelType type;

    @NotNull(message = "Creator ID is required")
    private UUID creatorId;
} 