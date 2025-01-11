package com.chatgenius.dto.request;

import com.chatgenius.model.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequest {
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Channel ID is required")
    private UUID channelId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Message type is required")
    private MessageType type;
    
    private UUID threadId;
} 