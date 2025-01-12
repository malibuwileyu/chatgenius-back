package com.chatgenius.service;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.model.Message;
import com.chatgenius.model.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message createMessage(CreateMessageRequest request);
    Message getMessage(UUID messageId);
    Page<Message> getChannelMessages(UUID channelId, Pageable pageable);
    List<Message> getLatestMessages(UUID channelId, int limit);
    Message updateMessage(UUID messageId, String content);
    void deleteMessage(UUID messageId);
    Message createReply(String content, UUID threadId, UUID channelId, UUID userId, MessageType type);
    List<Message> getThreadReplies(UUID threadId);
    List<Message> searchMessages(UUID channelId, String keyword);
    long getMessageCount(UUID channelId);
} 