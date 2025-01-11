package com.chatgenius.dto.response;

import com.chatgenius.model.Message;
import com.chatgenius.model.enums.MessageType;
import lombok.Data;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private String content;
    private UUID userId;
    private String username;
    private UUID channelId;
    private UUID threadId;
    private MessageType type;
    private ZonedDateTime createdAt;

    public static MessageResponse fromMessage(Message message) {
        return MessageResponse.builder()
            .id(message.getId())
            .content(message.getContent())
            .userId(message.getUser().getId())
            .username(message.getUser().getUsername())
            .channelId(message.getChannel().getId())
            .threadId(message.getThreadId())
            .type(message.getType())
            .createdAt(message.getCreatedAt())
            .build();
    }
} 