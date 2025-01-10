package com.chatgenius.websocket;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.service.ChannelService;
import com.chatgenius.service.UserService;
import com.chatgenius.websocket.handler.ChatWebSocketHandler;
import com.chatgenius.websocket.event.WebSocketEvents.ChannelEvent;
import com.chatgenius.websocket.event.WebSocketEvents.ErrorEvent;
import com.chatgenius.websocket.event.WebSocketEvents.TypingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChannelService channelService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    private UUID channelId;
    private UUID userId;
    private Channel testChannel;
    private User testUser;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        testChannel = new Channel();
        testChannel.setId(channelId);
        testChannel.setName("test-channel");
        
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("test-user");
        
        sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
    }

    @Test
    void handleChannelJoin_Success() {
        // Arrange
        when(channelService.getChannel(channelId)).thenReturn(testChannel);
        when(userService.getUser(userId)).thenReturn(testUser);

        // Act
        chatWebSocketHandler.handleChannelJoin(channelId, userId, headerAccessor);

        // Assert
        verify(channelService).addMember(channelId, userId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/channel." + channelId),
            any(ChannelEvent.class)
        );
        verify(headerAccessor.getSessionAttributes()).put("channel_" + channelId, true);
    }

    @Test
    void handleChannelLeave_Success() {
        // Arrange
        when(channelService.getChannel(channelId)).thenReturn(testChannel);
        when(userService.getUser(userId)).thenReturn(testUser);

        // Act
        chatWebSocketHandler.handleChannelLeave(channelId, userId, headerAccessor);

        // Assert
        verify(channelService).removeMember(channelId, userId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/channel." + channelId),
            any(ChannelEvent.class)
        );
        verify(headerAccessor.getSessionAttributes()).remove("channel_" + channelId);
    }

    @Test
    void handleTypingIndicator_Success() {
        // Arrange
        var typingEvent = new TypingEvent(userId, true);

        // Act
        chatWebSocketHandler.handleTypingIndicator(channelId, typingEvent);

        // Assert
        verify(messagingTemplate).convertAndSend(
            eq("/topic/typing." + channelId),
            eq(typingEvent)
        );
    }

    @Test
    void handleChannelJoin_Error() {
        // Arrange
        doThrow(new RuntimeException("Test error"))
            .when(channelService)
            .addMember(channelId, userId);

        // Act
        chatWebSocketHandler.handleChannelJoin(channelId, userId, headerAccessor);

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId.toString()),
            eq("/queue/errors"),
            any(ErrorEvent.class)
        );
    }

    @Test
    void handleChannelLeave_Error() {
        // Arrange
        doThrow(new RuntimeException("Test error"))
            .when(channelService)
            .removeMember(channelId, userId);

        // Act
        chatWebSocketHandler.handleChannelLeave(channelId, userId, headerAccessor);

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId.toString()),
            eq("/queue/errors"),
            any(ErrorEvent.class)
        );
    }
} 