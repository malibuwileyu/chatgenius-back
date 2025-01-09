package com.chatgenius.service;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    Channel createChannel(String name, ChannelType type, UUID creatorId);
    void addMemberToChannel(UUID channelId, UUID userId);
    void removeMemberFromChannel(UUID channelId, UUID userId);
    List<Channel> getUserChannels(UUID userId);
    List<Message> getChannelMessages(UUID channelId, int limit);
    Message sendMessage(UUID channelId, UUID userId, String content, MessageType type);
    void deleteMessage(UUID messageId, UUID userId);
    Channel createDirectMessageChannel(UUID user1Id, UUID user2Id);
    List<Channel> getDirectMessageChannels(UUID userId);
} 