package com.chatgenius.service;

import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.impl.ChannelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChannelServiceImpl channelService;

    private User testUser;
    private Channel testChannel;
    private UUID userId;
    private UUID channelId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        channelId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testChannel = new Channel();
        testChannel.setId(channelId);
        testChannel.setName("test-channel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.setCreatedAt(ZonedDateTime.now());
        testChannel.getMembers().add(testUser);
    }

    @Test
    void createChannel_Success() {
        CreateChannelRequest request = CreateChannelRequest.builder()
            .name("test-channel")
            .type(ChannelType.PUBLIC)
            .creatorId(userId)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        Channel channel = channelService.createChannel(request);
        assertNotNull(channel);
        assertEquals("test-channel", channel.getName());
        assertEquals(ChannelType.PUBLIC, channel.getType());
        assertTrue(channel.getMembers().contains(testUser));
    }

    @Test
    void getChannel_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));

        Channel channel = channelService.getChannel(channelId);
        assertNotNull(channel);
        assertEquals(channelId, channel.getId());
        assertEquals("test-channel", channel.getName());
    }

    @Test
    void getPublicChannels_Success() {
        List<Channel> channels = Arrays.asList(testChannel);
        when(channelRepository.findByType(ChannelType.PUBLIC)).thenReturn(channels);

        List<Channel> result = channelService.getPublicChannels();
        assertEquals(1, result.size());
        assertEquals("test-channel", result.get(0).getName());
    }

    @Test
    void addMember_Success() {
        User newMember = new User();
        newMember.setId(UUID.randomUUID());
        newMember.setUsername("newuser");
        newMember.setEmail("newuser@example.com");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(userRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        channelService.addMember(channelId, newMember.getId());
        assertTrue(testChannel.getMembers().contains(newMember));
    }

    @Test
    void removeMember_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        channelService.removeMember(channelId, userId);
        assertFalse(testChannel.getMembers().contains(testUser));
    }

    @Test
    void deleteChannel_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        doNothing().when(channelRepository).delete(testChannel);

        channelService.deleteChannel(channelId);
        verify(channelRepository).delete(testChannel);
    }

    @Test
    void getChannelMembers_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));

        List<UUID> members = channelService.getChannelMembers(channelId);
        assertEquals(1, members.size());
        assertEquals(userId, members.get(0));
    }
} 