package com.chatgenius.service.impl;

import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public ChatServiceImpl(ChannelRepository channelRepository,
                         UserRepository userRepository,
                         MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public Channel createChannel(String name, ChannelType type, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));

        Channel channel = new Channel();
        channel.setName(name);
        channel.setType(type);
        channel.setCreatedAt(ZonedDateTime.now());
        channel.getMembers().add(creator);

        return channelRepository.save(channel);
    }

    @Override
    public void addMemberToChannel(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + channelId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        channel.getMembers().add(user);
        channelRepository.save(channel);
    }

    @Override
    public void removeMemberFromChannel(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + channelId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        channel.getMembers().remove(user);
        channelRepository.save(channel);
    }

    @Override
    public List<Channel> getUserChannels(UUID userId) {
        return channelRepository.findByMemberId(userId);
    }

    @Override
    public List<Message> getChannelMessages(UUID channelId, int limit) {
        return messageRepository.findByChannelId(
            channelId,
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    @Override
    public Message sendMessage(UUID channelId, UUID userId, String content, MessageType type) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + channelId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Message message = new Message();
        message.setContent(content);
        message.setType(type);
        message.setUser(user);
        message.setChannel(channel);
        message.setCreatedAt(ZonedDateTime.now());

        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        if (!message.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this message");
        }

        messageRepository.delete(message);
    }

    @Override
    public Channel createDirectMessageChannel(UUID user1Id, UUID user2Id) {
        User user1 = userRepository.findById(user1Id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user1Id));
        User user2 = userRepository.findById(user2Id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user2Id));

        Channel channel = new Channel();
        channel.setName(user1.getUsername() + "-" + user2.getUsername());
        channel.setType(ChannelType.DIRECT_MESSAGE);
        channel.setCreatedAt(ZonedDateTime.now());
        channel.getMembers().add(user1);
        channel.getMembers().add(user2);

        return channelRepository.save(channel);
    }

    @Override
    public List<Channel> getDirectMessageChannels(UUID userId) {
        return channelRepository.findDirectMessageChannels(userId);
    }
} 