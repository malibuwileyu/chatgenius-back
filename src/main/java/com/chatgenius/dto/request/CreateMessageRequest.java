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
    private UUID channelId;
    private UUID userId;
    private UUID threadId;
    
    @NotBlank(message = "Message content is required")
    private String content;
    
    @NotNull(message = "Message type is required")
    private MessageType type;
} 