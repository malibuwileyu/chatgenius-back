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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, 
                            ChannelRepository channelRepository,
                            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Message createMessage(CreateMessageRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Message content cannot be empty");
        }
        if (request.getType() == null) {
            throw new ValidationException("Message type must be specified");
        }

        Channel channel = channelRepository.findById(request.getChannelId())
            .orElseThrow(() -> new ResourceNotFoundException("Channel", request.getChannelId().toString()));
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId().toString()));

        // Verify user is member of channel
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
            Message thread = getMessage(request.getThreadId());
            message.setThread(thread);
        }

        return messageRepository.save(message);
    }

    @Override
    public Message getMessage(UUID messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message", messageId.toString()));
    }

    @Override
    public Page<Message> getChannelMessages(UUID channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", channelId.toString());
        }
        return messageRepository.findByChannelId(channelId, pageable);
    }

    @Override
    public List<Message> getLatestMessages(UUID channelId, int limit) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", channelId.toString());
        }
        if (limit <= 0) {
            throw new ValidationException("Limit must be greater than 0");
        }
        return messageRepository.findLatestInChannel(channelId, 
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
            .getContent();
    }

    @Override
    public Message updateMessage(UUID messageId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Message content cannot be empty");
        }

        Message message = getMessage(messageId);
        message.setContent(content);
        message.setUpdatedAt(ZonedDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("Message", messageId.toString());
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
            .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId.toString()));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Verify user is member of channel
        if (!channel.getMembers().contains(user)) {
            throw new ValidationException("User is not a member of this channel");
        }

        // Verify reply is in same channel as parent
        if (!parentMessage.getChannel().getId().equals(channelId)) {
            throw new ValidationException("Reply must be in the same channel as the parent message");
        }

        Message reply = new Message();
        reply.setContent(content);
        reply.setChannel(channel);
        reply.setUser(user);
        reply.setType(type);
        reply.setThread(parentMessage);
        reply.setCreatedAt(ZonedDateTime.now());

        return messageRepository.save(reply);
    }

    @Override
    public List<Message> getThreadReplies(UUID threadId) {
        if (!messageRepository.existsById(threadId)) {
            throw new ResourceNotFoundException("Thread", threadId.toString());
        }
        return messageRepository.findRepliesByThreadId(threadId);
    }

    @Override
    public List<Message> searchMessages(UUID channelId, String keyword) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", channelId.toString());
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }
        return messageRepository.searchInChannel(channelId, keyword);
    }

    @Override
    public long getMessageCount(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", channelId.toString());
        }
        return messageRepository.countByChannelId(channelId);
    }
} 