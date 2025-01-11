package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;

    private UUID userId;
    private UUID channelId;
    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        channelId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .createdAt(ZonedDateTime.now())
                .build();

        CreateChannelRequest request = CreateChannelRequest.builder()
                .name("test-channel")
                .type(ChannelType.PUBLIC)
                .build();

        testChannel = Channel.builder()
                .id(channelId)
                .name(request.getName())
                .type(request.getType())
                .createdAt(ZonedDateTime.now())
                .build();
        testChannel.getMembers().add(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArguments()[0]);
    }
} 