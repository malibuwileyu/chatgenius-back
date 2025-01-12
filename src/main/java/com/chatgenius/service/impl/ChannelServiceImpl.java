package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.User;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.service.ChannelService;
import com.chatgenius.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.ZonedDateTime;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public ChannelServiceImpl(ChannelRepository channelRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Channel createChannel(CreateChannelRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Channel channel = Channel.builder()
                .name(request.getName())
                .type(request.getType())
                .createdAt(ZonedDateTime.now())
                .members(new HashSet<>())
                .build();
        
        channel.getMembers().add(user);
        return channelRepository.save(channel);
    }

    @Override
    @Transactional
    public void addMember(UUID channelId, String username) {
        Channel channel = getChannelById(channelId);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        channel.getMembers().add(user);
        channelRepository.save(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannelById(UUID id) {
        return channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteChannel(UUID id) {
        Channel channel = getChannelById(id);
        messageRepository.deleteByChannelId(id);
        channelRepository.delete(channel);
    }

    @Override
    @Transactional
    public Channel updateChannel(UUID id, Channel channel) {
        Channel existingChannel = getChannelById(id);
        existingChannel.setName(channel.getName());
        existingChannel.setType(channel.getType());
        return channelRepository.save(existingChannel);
    }

    @Override
    @Transactional
    public void removeMember(UUID channelId, String username) {
        Channel channel = getChannelById(channelId);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        channel.getMembers().remove(user);
        channelRepository.save(channel);
    }
} 