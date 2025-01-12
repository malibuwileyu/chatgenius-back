package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.exception.ValidationException;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public Message createMessage(CreateMessageRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Message content cannot be empty");
        }
        if (request.getType() == null) {
            throw new ValidationException("Message type must be specified");
        }

        Channel channel = channelRepository.findById(request.getChannelId())
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + request.getChannelId()));
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        if (!channel.getMembers().contains(user)) {
            throw new ValidationException("User is not a member of this channel");
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setChannel(channel);
        message.setUser(user);
        message.setType(request.getType());
        message.setCreatedAt(ZonedDateTime.now());

        if (request.getThreadId() != null) {
            message.setThreadId(request.getThreadId());
        }

        return messageRepository.save(message);
    }

    @Override
    public Message getMessage(UUID messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
    }

    @Override
    public Page<Message> getChannelMessages(UUID channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel not found: " + channelId);
        }
        List<Message> messages = messageRepository.findByChannelId(channelId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        return new PageImpl<>(messages.subList(start, end), pageable, messages.size());
    }

    @Override
    public List<Message> getLatestMessages(UUID channelId, int limit) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel not found: " + channelId);
        }
        if (limit <= 0) {
            throw new ValidationException("Limit must be greater than 0");
        }
        List<Message> messages = messageRepository.findByChannelId(channelId);
        return messages.subList(0, Math.min(limit, messages.size()));
    }

    @Override
    public Message updateMessage(UUID messageId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Message content cannot be empty");
        }

        Message message = getMessage(messageId);
        message.setContent(content);
        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("Message not found: " + messageId);
        }
        messageRepository.deleteById(messageId);
    }

    @Override
    public Message createReply(String content, UUID threadId, UUID channelId, UUID userId, MessageType type) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Reply content cannot be empty");
        }
        if (type == null) {
            throw new ValidationException("Message type must be specified");
        }

        Message parentMessage = getMessage(threadId);
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + channelId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!channel.getMembers().contains(user)) {
            throw new ValidationException("User is not a member of this channel");
        }

        if (!parentMessage.getChannel().getId().equals(channelId)) {
            throw new ValidationException("Reply must be in the same channel as the parent message");
        }

        Message reply = new Message();
        reply.setContent(content);
        reply.setChannel(channel);
        reply.setUser(user);
        reply.setType(type);
        reply.setThreadId(threadId);
        reply.setCreatedAt(ZonedDateTime.now());

        return messageRepository.save(reply);
    }

    @Override
    public List<Message> getThreadReplies(UUID threadId) {
        if (!messageRepository.existsById(threadId)) {
            throw new ResourceNotFoundException("Thread not found: " + threadId);
        }
        return messageRepository.findByThreadId(threadId);
    }

    @Override
    public List<Message> searchMessages(UUID channelId, String keyword) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel not found: " + channelId);
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }
        return messageRepository.findByChannelId(channelId);
    }

    @Override
    public long getMessageCount(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel not found: " + channelId);
        }
        return messageRepository.findByChannelId(channelId).size();
    }
} 