package com.chatgenius.dto.request;

import com.chatgenius.model.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateMessageRequest {
    private UUID channelId;
    private UUID userId;
    private UUID threadId;
    private String content;
    private MessageType type;
} 