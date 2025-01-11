package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.enums.MessageType;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    Channel createChannel(CreateChannelRequest request);
    Channel getChannel(UUID channelId);
    void deleteChannel(UUID channelId);
    List<Channel> getPublicChannels();
    List<Channel> getUserChannels(UUID userId);
    List<UUID> getChannelMembers(UUID channelId);
    void addMember(UUID channelId, UUID userId);
    void removeMember(UUID channelId, UUID userId);
    Message sendMessage(UUID channelId, UUID userId, String content, MessageType type);
    Message sendThreadReply(UUID channelId, UUID threadId, UUID userId, String content);
    boolean isChannelMember(UUID channelId, UUID userId);
} 