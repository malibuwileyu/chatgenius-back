package com.chatgenius.websocket.handler;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.service.ChatService;
import com.chatgenius.service.MessageService;
import com.chatgenius.service.UserService;
import com.chatgenius.websocket.event.WebSocketEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String username = (String) session.getAttributes().get("username");
        
        log.info("WebSocket connection attempt - Session ID: {}, User: {}", sessionId, username);
        log.info("Connection Headers: {}", session.getHandshakeHeaders());
        log.info("Connection Attributes: {}", session.getAttributes());
        log.info("Remote Address: {}", session.getRemoteAddress());
        
        if (username == null) {
            log.error("No authenticated user found in session");
            session.close();
            return;
        }
        
        sessions.put(sessionId, session);
        log.info("WebSocket connection established and session stored. Session ID: {}, User: {}", sessionId, username);
        
        // Send welcome message
        sendEvent(session, new WebSocketEvent("connected", Map.of(
            "sessionId", sessionId,
            "username", username
        )));
        
        // Broadcast user online status
        broadcastToAll(new WebSocketEvent("presence:online", Map.of(
            "userId", username,
            "timestamp", System.currentTimeMillis()
        )));
        
        log.info("Sent welcome message to session: {}", sessionId);
    }

    private String getUsername(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        log.info("Handling message for session {}", sessionId);
        log.info("Received payload: {}", payload);
        
        try {
            // Parse the incoming message as a WebSocketEvent
            WebSocketEvent event = objectMapper.readValue(payload, WebSocketEvent.class);
            
            // Handle different event types
            switch (event.getType()) {
                case "chat:message":
                    handleChatMessage(session, event);
                    break;
                case "chat:typing":
                    handleTypingIndicator(session, event);
                    break;
                case "chat:join":
                    handleJoinChannel(session, event);
                    break;
                case "presence:status":
                    handlePresenceUpdate(session, event);
                    break;
                case "chat:list_channels":
                    handleListChannels(session);
                    break;
                default:
                    // Echo unknown event types back
                    sendEvent(session, new WebSocketEvent("echo", Map.of("original", event)));
                    break;
            }
            
            log.info("Successfully handled message of type: {} for session: {}", event.getType(), sessionId);
        } catch (Exception e) {
            log.error("Error handling message for session: {}", sessionId, e);
            sendEvent(session, new WebSocketEvent("error", Map.of(
                "message", "Failed to process message: " + e.getMessage(),
                "originalPayload", payload
            )));
        }
    }

    private void handleChatMessage(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String content = (String) data.get("content");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }

        try {
            UUID userId = getUserId(username);
            UUID channelUuid = UUID.fromString(channelId);

            log.info("Checking channel membership for user {} in channel {}", username, channelId);
            
            // Get channel to check membership
            List<Channel> userChannels = chatService.getUserChannels(userId);
            log.info("User {} has access to {} channels", username, userChannels.size());
            userChannels.forEach(channel -> 
                log.info("Channel: {} ({})", channel.getName(), channel.getId())
            );
            
            boolean isMember = userChannels.stream()
                .anyMatch(channel -> channel.getId().equals(channelUuid));
            log.info("User {} is{} a member of channel {}", username, isMember ? "" : " not", channelId);

            if (!isMember) {
                sendError(session, "You are not a member of this channel");
                return;
            }

            // Create and save the message
            Message message = chatService.sendMessage(
                channelUuid,
                userId,
                content,
                MessageType.TEXT
            );

            // Broadcast to all sessions in the channel
            WebSocketEvent response = new WebSocketEvent("chat:message", Map.of(
                "messageId", message.getId().toString(),
                "channelId", channelId,
                "userId", username,
                "content", content,
                "timestamp", message.getCreatedAt().toInstant().toEpochMilli()
            ));
            
            broadcastToChannel(channelId, response);
            
            log.info("Message saved and broadcast - Channel: {}, User: {}, MessageId: {}", 
                    channelId, username, message.getId());
        } catch (Exception e) {
            log.error("Error handling chat message", e);
            sendError(session, "Failed to send message: " + e.getMessage());
        }
    }

    private void handleTypingIndicator(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        boolean isTyping = (boolean) data.get("isTyping");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }
        
        WebSocketEvent response = new WebSocketEvent("chat:typing", Map.of(
            "channelId", channelId,
            "userId", username,
            "isTyping", isTyping
        ));
        
        broadcastToChannel(channelId, response);
    }

    private void handlePresenceUpdate(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String status = (String) data.get("status");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }
        
        WebSocketEvent response = new WebSocketEvent("presence:update", Map.of(
            "userId", username,
            "status", status,
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToAll(response);
    }

    private void handleListChannels(WebSocketSession session) throws Exception {
        String username = getUsername(session);
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }

        try {
            UUID userId = getUserId(username);
            List<Channel> channels = chatService.getUserChannels(userId);
            
            List<Map<String, Object>> channelList = channels.stream()
                .map(channel -> {
                    Map<String, Object> channelData = new ConcurrentHashMap<>();
                    channelData.put("id", channel.getId().toString());
                    channelData.put("name", channel.getName());
                    channelData.put("type", channel.getType().toString());
                    return channelData;
                })
                .collect(Collectors.toList());

            sendEvent(session, new WebSocketEvent("chat:channels", Map.of(
                "channels", channelList
            )));
            
            log.info("Sent channel list to user: {}", username);
        } catch (Exception e) {
            log.error("Error listing channels", e);
            sendError(session, "Failed to list channels: " + e.getMessage());
        }
    }

    private void handleJoinChannel(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }

        try {
            UUID userId = getUserId(username);
            UUID channelUuid = UUID.fromString(channelId);

            // Add user to channel
            chatService.addMember(channelUuid, userId);

            // Send success response
            sendEvent(session, new WebSocketEvent("chat:joined", Map.of(
                "channelId", channelId,
                "userId", username
            )));
            
            // Broadcast join event to channel members
            WebSocketEvent joinNotification = new WebSocketEvent("chat:user_joined", Map.of(
                "channelId", channelId,
                "userId", username,
                "timestamp", System.currentTimeMillis()
            ));
            broadcastToChannel(channelId, joinNotification);
            
            log.info("User {} joined channel {}", username, channelId);
        } catch (Exception e) {
            log.error("Error joining channel", e);
            sendError(session, "Failed to join channel: " + e.getMessage());
        }
    }

    private void sendEvent(WebSocketSession session, WebSocketEvent event) throws Exception {
        String json = objectMapper.writeValueAsString(event);
        session.sendMessage(new TextMessage(json));
    }

    private void broadcastToChannel(String channelId, WebSocketEvent event) throws Exception {
        String json = objectMapper.writeValueAsString(event);
        TextMessage message = new TextMessage(json);
        UUID channelUuid = UUID.fromString(channelId);
        
        try {
            // Broadcast to sessions of channel members
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    String sessionUsername = getUsername(session);
                    if (sessionUsername != null) {
                        UUID sessionUserId = getUserId(sessionUsername);
                        List<Channel> userChannels = chatService.getUserChannels(sessionUserId);
                        boolean isMember = userChannels.stream()
                            .anyMatch(c -> c.getId().equals(channelUuid));
                        
                        if (isMember) {
                            session.sendMessage(message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting to channel {}", channelId, e);
        }
    }

    private void broadcastToAll(WebSocketEvent event) throws Exception {
        String json = objectMapper.writeValueAsString(event);
        TextMessage message = new TextMessage(json);
        
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        log.info("WebSocket connection closing - Session ID: {}", sessionId);
        log.info("Close Status: {}", status);
        
        sessions.remove(sessionId);
        log.info("Session removed from active sessions. Session ID: {}", sessionId);
        
        // Broadcast offline status
        broadcastToAll(new WebSocketEvent("presence:offline", Map.of(
            "userId", sessionId,
            "timestamp", System.currentTimeMillis()
        )));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("Transport error for session: {}", sessionId, exception);
        log.error("Session details - Remote Address: {}, URI: {}", 
                 session.getRemoteAddress(), 
                 session.getUri());
        
        sendEvent(session, new WebSocketEvent("error", Map.of(
            "message", exception.getMessage()
        )));
    }

    private void sendError(WebSocketSession session, String message) throws Exception {
        sendEvent(session, new WebSocketEvent("error", Map.of("message", message)));
    }

    private UUID getUserId(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user.getId();
    }
} 