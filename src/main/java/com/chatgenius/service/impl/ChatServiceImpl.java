package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateChannelRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Channel createChannel(CreateChannelRequest request) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User creator = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Channel channel = new Channel();
        channel.setName(request.getName());
        channel.setType(request.getType());
        channel.setCreatedAt(ZonedDateTime.now());
        channel.getMembers().add(creator);
        
        return channelRepository.save(channel);
    }

    @Override
    public Channel getChannel(UUID channelId) {
        return channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
    }

    @Override
    @Transactional
    public void deleteChannel(UUID channelId) {
        Channel channel = getChannel(channelId);
        channelRepository.delete(channel);
    }

    @Override
    public List<Channel> getPublicChannels() {
        return channelRepository.findByType(ChannelType.PUBLIC);
    }

    @Override
    public List<Channel> getUserChannels(UUID userId) {
        return channelRepository.findByMembersId(userId);
    }

    @Override
    public List<UUID> getChannelMembers(UUID channelId) {
        Channel channel = getChannel(channelId);
        return channel.getMembers().stream()
            .map(User::getId)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(UUID channelId, UUID userId) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        channel.getMembers().add(user);
        channelRepository.save(channel);
    }

    @Override
    @Transactional
    public void removeMember(UUID channelId, UUID userId) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        channel.getMembers().remove(user);
        channelRepository.save(channel);
    }

    @Override
    @Transactional
    public Message sendMessage(UUID channelId, UUID userId, String content, MessageType type) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        if (!channel.getMembers().contains(user)) {
            throw new IllegalStateException("User is not a member of this channel");
        }

        Message message = new Message();
        message.setContent(content);
        message.setType(type);
        message.setUser(user);
        message.setChannel(channel);
        message.setCreatedAt(ZonedDateTime.now());
        
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public Message sendThreadReply(UUID channelId, UUID threadId, UUID userId, String content) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        if (!channel.getMembers().contains(user)) {
            throw new IllegalStateException("User is not a member of this channel");
        }

        Message threadMessage = messageRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread message not found"));
            
        if (!threadMessage.getChannel().getId().equals(channelId)) {
            throw new IllegalStateException("Thread does not belong to this channel");
        }

        Message reply = new Message();
        reply.setContent(content);
        reply.setType(MessageType.THREAD_REPLY);
        reply.setUser(user);
        reply.setChannel(channel);
        reply.setThreadId(threadId);
        reply.setCreatedAt(ZonedDateTime.now());
        
        return messageRepository.save(reply);
    }

    @Override
    public boolean isChannelMember(UUID channelId, UUID userId) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        return channel.getMembers().contains(user);
    }
} 