package com.chatgenius.dto.request;

import com.chatgenius.model.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateChannelRequest {
    @NotBlank(message = "Channel name is required")
    private String name;

    @NotNull(message = "Channel type is required")
    private ChannelType type;

    private UUID creatorId;
} 