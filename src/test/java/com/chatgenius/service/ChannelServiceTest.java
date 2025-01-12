package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.impl.ChannelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private UserRepository userRepository;

    private ChannelService channelService;
    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        channelService = new ChannelServiceImpl(channelRepository, messageRepository, userRepository);
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setCreatedAt(ZonedDateTime.now());
        testUser.setLastSeenAt(ZonedDateTime.now());
        
        testChannel = new Channel();
        testChannel.setId(UUID.randomUUID());
        testChannel.setName("test-channel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.setMembers(new HashSet<>());
        testChannel.setCreatedAt(ZonedDateTime.now());
    }

    @Test
    void createChannel_Success() {
        CreateChannelRequest request = new CreateChannelRequest();
        request.setName(testChannel.getName());
        request.setType(ChannelType.PUBLIC);

        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> {
            Channel savedChannel = invocation.getArgument(0);
            savedChannel.setId(testChannel.getId());
            return savedChannel;
        });

        Channel createdChannel = channelService.createChannel(request, testUser.getUsername());

        assertNotNull(createdChannel);
        assertEquals(testChannel.getName(), createdChannel.getName());
        assertTrue(createdChannel.getMembers().contains(testUser));
    }

    @Test
    void getChannelById_Success() {
        UUID channelId = testChannel.getId();
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));

        Channel foundChannel = channelService.getChannelById(channelId);

        assertNotNull(foundChannel);
        assertEquals(channelId, foundChannel.getId());
        assertEquals(testChannel.getName(), foundChannel.getName());
    }
} 