package com.chatgenius.websocket.handler;

import com.chatgenius.model.User;
import com.chatgenius.service.PresenceService;
import com.chatgenius.service.UserService;
import com.chatgenius.websocket.event.WebSocketEvents.PresenceEvent;
import com.chatgenius.websocket.event.WebSocketEvents.ErrorEvent;
import com.chatgenius.websocket.event.WebSocketEvents.PresenceUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PresenceWebSocketHandler.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;
    private final UserService userService;

    @MessageMapping("/presence.update")
    public void handlePresenceUpdate(@Payload PresenceUpdate presenceUpdate,
                                   SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Handling presence update for user: {}", presenceUpdate.userId());
        
        try {
            // Update user's presence status
            presenceService.updateUserPresence(presenceUpdate.userId(), presenceUpdate.status());
            
            // Get updated user details
            User user = userService.getUser(presenceUpdate.userId());
            
            // Broadcast presence update to all connected clients
            messagingTemplate.convertAndSend(
                "/topic/presence",
                new PresenceEvent("presence:updated", user, presenceUpdate.status())
            );
            
            // Store user's status in session
            headerAccessor.getSessionAttributes().put("user_status", presenceUpdate.status());
            
            logger.info("User {} presence updated to {}", presenceUpdate.userId(), presenceUpdate.status());
        } catch (Exception e) {
            logger.error("Error handling presence update", e);
            messagingTemplate.convertAndSendToUser(
                presenceUpdate.userId().toString(),
                "/queue/errors",
                new ErrorEvent("Failed to update presence: " + e.getMessage())
            );
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.debug("Received a new web socket connection");
        
        // Extract user information from the session
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String userId = headers.getUser().getName(); // Assuming user ID is stored in principal
        
        try {
            presenceService.updateUserPresence(UUID.fromString(userId), "online");
            User user = userService.getUser(UUID.fromString(userId));
            
            // Broadcast user's online status
            messagingTemplate.convertAndSend(
                "/topic/presence",
                new PresenceEvent("presence:updated", user, "online")
            );
            
            logger.info("User {} connected and marked as online", userId);
        } catch (Exception e) {
            logger.error("Error handling websocket connect", e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        logger.debug("User disconnected from web socket connection");
        
        // Extract user information from the session
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String userId = headers.getUser().getName(); // Assuming user ID is stored in principal
        
        try {
            presenceService.handleDisconnect(UUID.fromString(userId));
            User user = userService.getUser(UUID.fromString(userId));
            
            // Broadcast user's offline status
            messagingTemplate.convertAndSend(
                "/topic/presence",
                new PresenceEvent("presence:updated", user, "offline")
            );
            
            logger.info("User {} disconnected and marked as offline", userId);
        } catch (Exception e) {
            logger.error("Error handling websocket disconnect", e);
        }
    }
} 