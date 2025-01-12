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
        String username = "testuser";
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();
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
        channel.getMembers().add(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(channelRepository.save(any(Channel.class))).thenReturn(channel);

        Channel result = channelService.createChannel(request, username);

        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getType(), result.getType());
        assertTrue(result.getMembers().contains(user));
        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    void getChannelById_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));

        Channel channel = channelService.getChannelById(channelId);
        assertNotNull(channel);
        assertEquals(channelId, channel.getId());
        assertEquals("test-channel", channel.getName());
    }

    @Test
    void getAllChannels_Success() {
        List<Channel> channels = Arrays.asList(testChannel);
        when(channelRepository.findAll()).thenReturn(channels);

        List<Channel> result = channelService.getAllChannels();
        assertEquals(1, result.size());
        assertEquals("test-channel", result.get(0).getName());
    }

    @Test
    void addMember_Success() {
        String username = "newuser";
        User newMember = new User();
        newMember.setId(UUID.randomUUID());
        newMember.setUsername(username);
        newMember.setEmail("newuser@example.com");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(newMember));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        channelService.addMember(channelId, username);
        assertTrue(testChannel.getMembers().contains(newMember));
    }

    @Test
    void removeMember_Success() {
        String username = "testuser";
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        channelService.removeMember(channelId, username);
        assertFalse(testChannel.getMembers().contains(testUser));
    }

    @Test
    void deleteChannel_Success() {
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        doNothing().when(channelRepository).delete(testChannel);

        channelService.deleteChannel(channelId);
        verify(channelRepository).delete(testChannel);
    }
} 