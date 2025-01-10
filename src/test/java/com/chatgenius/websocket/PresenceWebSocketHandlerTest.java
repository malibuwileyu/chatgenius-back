package com.chatgenius.websocket;

import com.chatgenius.model.User;
import com.chatgenius.service.PresenceService;
import com.chatgenius.service.UserService;
import com.chatgenius.websocket.handler.PresenceWebSocketHandler;
import com.chatgenius.websocket.event.WebSocketEvents.PresenceEvent;
import com.chatgenius.websocket.event.WebSocketEvents.ErrorEvent;
import com.chatgenius.websocket.event.WebSocketEvents.PresenceUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PresenceService presenceService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock
    private Message<byte[]> message;

    @Mock
    private Principal principal;

    @InjectMocks
    private PresenceWebSocketHandler presenceWebSocketHandler;

    private UUID userId;
    private User testUser;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("test-user");
        
        sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        when(headerAccessor.getUser()).thenReturn(principal);
        when(principal.getName()).thenReturn(userId.toString());
    }

    @Test
    void handlePresenceUpdate_Success() {
        // Arrange
        var presenceUpdate = new PresenceUpdate(userId, "online");
        when(userService.getUser(userId)).thenReturn(testUser);

        // Act
        presenceWebSocketHandler.handlePresenceUpdate(presenceUpdate, headerAccessor);

        // Assert
        verify(presenceService).updateUserPresence(userId, "online");
        verify(messagingTemplate).convertAndSend(
            eq("/topic/presence"),
            any(PresenceEvent.class)
        );
        verify(headerAccessor.getSessionAttributes()).put("user_status", "online");
    }

    @Test
    void handlePresenceUpdate_Error() {
        // Arrange
        var presenceUpdate = new PresenceUpdate(userId, "online");
        doThrow(new RuntimeException("Test error"))
            .when(presenceService)
            .updateUserPresence(userId, "online");

        // Act
        presenceWebSocketHandler.handlePresenceUpdate(presenceUpdate, headerAccessor);

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId.toString()),
            eq("/queue/errors"),
            any(ErrorEvent.class)
        );
    }

    @Test
    void handleWebSocketConnect_Success() {
        // Arrange
        when(userService.getUser(userId)).thenReturn(testUser);
        var connectEvent = new SessionConnectEvent(this, message);
        when(SimpMessageHeaderAccessor.wrap(message)).thenReturn(headerAccessor);

        // Act
        presenceWebSocketHandler.handleWebSocketConnectListener(connectEvent);

        // Assert
        verify(presenceService).updateUserPresence(userId, "online");
        verify(messagingTemplate).convertAndSend(
            eq("/topic/presence"),
            any(PresenceEvent.class)
        );
    }

    @Test
    void handleWebSocketDisconnect_Success() {
        // Arrange
        when(userService.getUser(userId)).thenReturn(testUser);
        var disconnectEvent = new SessionDisconnectEvent(
            this,
            message,
            "1",
            CloseStatus.NORMAL
        );
        when(SimpMessageHeaderAccessor.wrap(message)).thenReturn(headerAccessor);

        // Act
        presenceWebSocketHandler.handleWebSocketDisconnectListener(disconnectEvent);

        // Assert
        verify(presenceService).handleDisconnect(userId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/presence"),
            any(PresenceEvent.class)
        );
    }
} 