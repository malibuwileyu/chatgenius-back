package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import java.util.List;
import java.util.UUID;

public interface ChannelService {
    List<Channel> getAllChannels();
    Channel getChannelById(UUID id);
    Channel createChannel(CreateChannelRequest request, String username);
    void deleteChannel(UUID id);
    Channel updateChannel(UUID id, Channel channel);
    void addMember(UUID channelId, String username);
    void removeMember(UUID channelId, String username);
} 