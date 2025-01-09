package com.chatgenius.dto.request;

import com.chatgenius.model.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateReplyRequest {
    private String content;
    private UUID threadId;
    private UUID channelId;
    private UUID userId;
    private MessageType type;
} 