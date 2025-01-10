package com.chatgenius.dto.response;

import com.chatgenius.model.Message;
import com.chatgenius.model.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageResponse extends BaseResponse {
    private static final Logger logger = LoggerFactory.getLogger(MessageResponse.class);
    
    private String content;
    private MessageType type;
    private UserResponse user;
    private ChannelResponse channel;
    private MessageResponse thread;
    private List<MessageResponse> replies;
    private int reactionCount;
    private ZonedDateTime updatedAt;

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static MessageResponse fromMessage(Message message) {
        if (message == null) {
            logger.error("Cannot create MessageResponse from null message");
            throw new IllegalArgumentException("Message cannot be null");
        }

        try {
            MessageResponse response = new MessageResponse();
            response.setId(message.getId());
            response.setContent(message.getContent());
            response.setType(message.getType());
            response.setCreatedAt(message.getCreatedAt());
            response.setUpdatedAt(message.getUpdatedAt());
            
            if (message.getUser() != null) {
                response.setUser(UserResponse.fromUser(message.getUser()));
            }

            if (message.getChannel() != null) {
                response.setChannel(ChannelResponse.fromChannel(message.getChannel()));
            }
            
            if (message.getThread() != null) {
                response.setThread(MessageResponse.fromMessage(message.getThread()));
            }

            if (message.getReplies() != null) {
                response.setReplies(message.getReplies().stream()
                        .filter(reply -> reply != null)
                        .map(MessageResponse::fromMessage)
                        .collect(Collectors.toList()));
            } else {
                response.setReplies(new ArrayList<>());
            }
            
            if (message.getReactions() != null) {
                response.setReactionCount(message.getReactions().size());
            }

            return response;
        } catch (Exception e) {
            logger.error("Error creating MessageResponse: {}", e.getMessage(), e);
            throw e;
        }
    }
} 