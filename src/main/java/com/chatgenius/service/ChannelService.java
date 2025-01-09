package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createChannel(CreateChannelRequest request);
    void addMember(UUID channelId, UUID userId);
    List<Channel> getPublicChannels();
    Channel getChannel(UUID id);
    void deleteChannel(UUID id);
    List<UUID> getChannelMembers(UUID id);
    void removeMember(UUID channelId, UUID userId);
} 