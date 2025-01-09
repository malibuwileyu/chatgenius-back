package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.User;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.ChannelService;
import com.chatgenius.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChannelServiceImpl(ChannelRepository channelRepository, UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Channel createChannel(CreateChannelRequest request) {
        Channel channel = new Channel();
        channel.setName(request.getName());
        channel.setType(request.getType());
        
        if (request.getCreatorId() != null) {
            User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getCreatorId()));
            channel.getMembers().add(creator);
        }
        
        return channelRepository.save(channel);
    }

    @Override
    @Transactional
    public void addMember(UUID channelId, UUID userId) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        channel.getMembers().add(user);
        channelRepository.save(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getPublicChannels() {
        return channelRepository.findByType(ChannelType.PUBLIC);
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannel(UUID id) {
        return channelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteChannel(UUID id) {
        Channel channel = getChannel(id);
        channelRepository.delete(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getChannelMembers(UUID id) {
        Channel channel = getChannel(id);
        return channel.getMembers().stream()
            .map(User::getId)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(UUID channelId, UUID userId) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        channel.getMembers().remove(user);
        channelRepository.save(channel);
    }
} 