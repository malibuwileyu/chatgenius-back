package com.chatgenius.controller;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {
    @Mock
    private ChannelService channelService;
    @InjectMocks
    private ChannelController channelController;

    @Test
    void createChannel_Success() {
        // Given
        String username = "testuser";
        CreateChannelRequest request = CreateChannelRequest.builder()
                .name("test-channel")
                .type(ChannelType.PUBLIC)
                .build();
        Channel channel = Channel.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .type(request.getType())
                .createdAt(ZonedDateTime.now())
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(channelService.createChannel(request, username)).thenReturn(channel);

        // When
        ResponseEntity<Channel> response = channelController.createChannel(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(channel, response.getBody());
        verify(channelService).createChannel(request, username);
    }

    @Test
    void addMember_Success() {
        // Given
        UUID channelId = UUID.randomUUID();
        String username = "testuser";

        doNothing().when(channelService).addMember(channelId, username);

        // When
        ResponseEntity<Void> response = channelController.addMember(channelId, username);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(channelService).addMember(channelId, username);
    }
} 