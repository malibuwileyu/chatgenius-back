package com.chatgenius.websocket.event;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;

import java.util.UUID;

public class WebSocketEvents {
    public record ChannelEvent(String type, Channel channel, User user) {}
    public record ErrorEvent(String message) {}
    public record TypingEvent(UUID userId, boolean isTyping) {}
    public record PresenceEvent(String type, User user, String status) {}
    public record PresenceUpdate(UUID userId, String status) {}
} 