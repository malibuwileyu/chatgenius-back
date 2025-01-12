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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> channelSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivityTimestamp = new ConcurrentHashMap<>();
    private static final long INACTIVE_TIMEOUT = 1800000; // 30 minutes in milliseconds

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String username = getUsername(session);
        
        log.info("WebSocket connection attempt - Session ID: {}, User: {}", sessionId, username);
        
        if (username == null) {
            log.error("No authenticated user found in session");
            session.close();
            return;
        }
        
        sessions.put(sessionId, session);
        updateLastActivity(sessionId);
        
        sendEvent(session, new WebSocketEvent("connected", Map.of(
            "sessionId", sessionId,
            "username", username
        )));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        updateLastActivity(sessionId);
        
        try {
            WebSocketEvent event = objectMapper.readValue(payload, WebSocketEvent.class);
            validateEvent(event);
            
            switch (event.getType()) {
                case "ping":
                    log.debug("Received ping from session: {}", sessionId);
                    // Respond with pong
                    WebSocketEvent pongEvent = new WebSocketEvent("pong", Map.of(
                        "timestamp", System.currentTimeMillis()
                    ));
                    log.debug("Sending pong to session: {}", sessionId);
                    sendEvent(session, pongEvent);
                    break;
                case "chat:message":
                    handleChatMessage(session, event);
                    break;
                case "chat:thread:create":
                    handleThreadCreate(session, event);
                    break;
                case "chat:thread:reply":
                    handleThreadReply(session, event);
                    break;
                case "chat:thread:list":
                    handleThreadList(session, event);
                    break;
                case "chat:typing":
                    handleTypingIndicator(session, event);
                    break;
                case "chat:join":
                    handleJoinChannel(session, event);
                    break;
                case "chat:leave":
                    handleLeaveChannel(session, event);
                    break;
                case "chat:list_channels":
                    handleListChannels(session);
                    break;
                case "chat:message:delete":
                    handleMessageDelete(session, event);
                    break;
                default:
                    handleUnknownEvent(session, event);
                    break;
            }
            
            log.info("Successfully handled message type: {} for session: {}", event.getType(), sessionId);
        } catch (Exception e) {
            handleMessageError(session, e, payload);
        }
    }

    private void handleChatMessage(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String content = (String) data.get("content");
        String username = getUsername(session);
        
        if (!validateUserAndChannel(username, channelId)) {
            sendError(session, "Invalid user or channel");
            return;
        }
        
        UUID userId = getUserId(username);
        UUID channelUuid = UUID.fromString(channelId);
        UUID tempMessageId = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        
        // Send immediate optimistic response
        WebSocketEvent optimisticResponse = new WebSocketEvent("chat:message:pending", Map.of(
            "messageId", tempMessageId.toString(),
            "channelId", channelId,
            "userId", username,
            "content", content,
            "timestamp", timestamp
        ));
        
        broadcastToChannel(channelId, optimisticResponse);
        
        try {
            // Attempt to persist the message
            Message message = chatService.sendMessage(
                channelUuid,
                userId,
                content,
                MessageType.TEXT
            );
            
            // Send confirmation with actual message ID
            WebSocketEvent confirmation = new WebSocketEvent("chat:message:confirmed", Map.of(
                "tempMessageId", tempMessageId.toString(),
                "messageId", message.getId().toString(),
                "channelId", channelId,
                "userId", username,
                "content", content,
                "timestamp", message.getCreatedAt().toInstant().toEpochMilli()
            ));
            
            broadcastToChannel(channelId, confirmation);
            
        } catch (Exception e) {
            // If persistence fails, send rollback event
            WebSocketEvent rollback = new WebSocketEvent("chat:message:failed", Map.of(
                "messageId", tempMessageId.toString(),
                "channelId", channelId,
                "reason", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
            
            broadcastToChannel(channelId, rollback);
            throw e;
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
        
        UUID userId = getUserId(username);
        UUID channelUuid = UUID.fromString(channelId);
        
        chatService.addMember(channelUuid, userId);
        addToChannelSubscriptions(session.getId(), channelId);
        
        WebSocketEvent response = new WebSocketEvent("chat:joined", Map.of(
            "channelId", channelId,
            "userId", username,
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToChannel(channelId, response);
    }

    private void handleLeaveChannel(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }
        
        removeFromChannelSubscriptions(session.getId(), channelId);
        
        WebSocketEvent response = new WebSocketEvent("chat:left", Map.of(
            "channelId", channelId,
            "userId", username,
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToChannel(channelId, response);
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

    private void handleListChannels(WebSocketSession session) throws Exception {
        String username = getUsername(session);
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }

        UUID userId = getUserId(username);
        List<Channel> channels = chatService.getUserChannels(userId);
        
        List<Map<String, Object>> channelList = channels.stream()
            .map(channel -> {
                Map<String, Object> channelData = new HashMap<>();
                channelData.put("id", channel.getId().toString());
                channelData.put("name", channel.getName());
                channelData.put("type", channel.getType().toString());
                return channelData;
            })
            .collect(Collectors.toList());

        sendEvent(session, new WebSocketEvent("chat:channels", Map.of(
            "channels", channelList
        )));
    }

    private void handleUnknownEvent(WebSocketSession session, WebSocketEvent event) throws IOException {
        log.warn("Received unknown event type: {}", event.getType());
        sendError(session, "Unknown event type: " + event.getType());
    }

    private void handleMessageError(WebSocketSession session, Exception e, String payload) {
        log.error("Error handling message: {} - {}", payload, e.getMessage());
        try {
            sendError(session, "Error processing message: " + e.getMessage());
        } catch (IOException ex) {
            log.error("Failed to send error message to client", ex);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        channelSubscriptions.remove(sessionId);
        lastActivityTimestamp.remove(sessionId);
        log.info("WebSocket connection closed - Session ID: {}, Status: {}", sessionId, status);
    }

    private void validateEvent(WebSocketEvent event) {
        if (event == null || event.getType() == null) {
            throw new IllegalArgumentException("Invalid event format");
        }
        // Allow ping events without data
        if (!event.getType().equals("ping") && event.getData() == null) {
            throw new IllegalArgumentException("Invalid event format");
        }
    }

    private boolean validateUserAndChannel(String username, String channelId) {
        return username != null && channelId != null && !channelId.trim().isEmpty();
    }

    private String getUsername(WebSocketSession session) {
        if (session == null || !session.getAttributes().containsKey("authenticated") || 
            !session.getAttributes().containsKey("username")) {
            return null;
        }
        
        Boolean authenticated = (Boolean) session.getAttributes().get("authenticated");
        if (!authenticated) {
            return null;
        }
        
        return (String) session.getAttributes().get("username");
    }

    private UUID getUserId(String username) {
        User user = userService.findByUsername(username);
        return user != null ? user.getId() : null;
    }

    private void sendEvent(WebSocketSession session, WebSocketEvent event) throws IOException {
        if (session.isOpen()) {
            String payload = objectMapper.writeValueAsString(event);
            session.sendMessage(new TextMessage(payload));
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        WebSocketEvent errorEvent = new WebSocketEvent("error", Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
        sendEvent(session, errorEvent);
    }

    private void broadcastToChannel(String channelId, WebSocketEvent event) {
        Set<String> subscribers = channelSubscriptions.getOrDefault(channelId, Collections.emptySet());
        subscribers.stream()
            .map(sessions::get)
            .filter(Objects::nonNull)
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    sendEvent(session, event);
                } catch (IOException e) {
                    log.error("Failed to broadcast message to session: {}", session.getId(), e);
                }
            });
    }

    private void addToChannelSubscriptions(String sessionId, String channelId) {
        channelSubscriptions.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    private void removeFromChannelSubscriptions(String sessionId, String channelId) {
        Set<String> subscribers = channelSubscriptions.get(channelId);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                channelSubscriptions.remove(channelId);
            }
        }
    }

    private void updateLastActivity(String sessionId) {
        lastActivityTimestamp.put(sessionId, System.currentTimeMillis());
    }

    private void handleThreadCreate(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String content = (String) data.get("content");
        String username = getUsername(session);
        
        if (!validateUserAndChannel(username, channelId)) {
            sendError(session, "Invalid user or channel");
            return;
        }
        
        UUID userId = getUserId(username);
        UUID channelUuid = UUID.fromString(channelId);
        
        Message threadMessage = chatService.sendMessage(
            channelUuid,
            userId,
            content,
            MessageType.THREAD_START
        );
        
        WebSocketEvent response = new WebSocketEvent("chat:thread:created", Map.of(
            "messageId", threadMessage.getId().toString(),
            "channelId", channelId,
            "userId", username,
            "content", content,
            "timestamp", threadMessage.getCreatedAt().toInstant().toEpochMilli()
        ));
        
        broadcastToChannel(channelId, response);
    }

    private void handleThreadReply(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String channelId = (String) data.get("channelId");
        String threadId = (String) data.get("threadId");
        String content = (String) data.get("content");
        String username = getUsername(session);
        
        if (!validateUserAndChannel(username, channelId)) {
            sendError(session, "Invalid user or channel");
            return;
        }
        
        UUID userId = getUserId(username);
        UUID channelUuid = UUID.fromString(channelId);
        UUID threadUuid = UUID.fromString(threadId);
        
        Message reply = chatService.sendThreadReply(
            channelUuid,
            threadUuid,
            userId,
            content
        );
        
        WebSocketEvent response = new WebSocketEvent("chat:thread:reply", Map.of(
            "messageId", reply.getId().toString(),
            "threadId", threadId,
            "channelId", channelId,
            "userId", username,
            "content", content,
            "timestamp", reply.getCreatedAt().toInstant().toEpochMilli()
        ));
        
        broadcastToChannel(channelId, response);
    }

    private void handleThreadList(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String threadId = (String) data.get("threadId");
        String username = getUsername(session);
        
        if (username == null) {
            sendError(session, "Not authenticated");
            return;
        }
        
        UUID threadUuid = UUID.fromString(threadId);
        List<Message> replies = messageService.getThreadReplies(threadUuid);
        
        List<Map<String, Object>> messageList = replies.stream()
            .map(message -> {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("messageId", message.getId().toString());
                messageData.put("threadId", threadId);
                messageData.put("channelId", message.getChannel().getId().toString());
                messageData.put("userId", message.getUser().getUsername());
                messageData.put("content", message.getContent());
                messageData.put("type", message.getType().toString());
                messageData.put("timestamp", message.getCreatedAt().toInstant().toEpochMilli());
                return messageData;
            })
            .collect(Collectors.toList());

        sendEvent(session, new WebSocketEvent("chat:thread:messages", Map.of(
            "threadId", threadId,
            "messages", messageList
        )));
    }

    private void handleMessageDelete(WebSocketSession session, WebSocketEvent event) throws Exception {
        Map<String, Object> data = event.getData();
        String messageId = (String) data.get("messageId");
        String channelId = (String) data.get("channelId");
        String username = getUsername(session);
        
        if (!validateUserAndChannel(username, channelId)) {
            sendError(session, "Invalid user or channel");
            return;
        }

        // Broadcast deletion event
        WebSocketEvent response = new WebSocketEvent("chat:message:deleted", Map.of(
            "messageId", messageId,
            "channelId", channelId,
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToChannel(channelId, response);
    }
} 