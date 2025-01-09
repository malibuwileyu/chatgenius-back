package com.chatgenius.service;

import com.chatgenius.config.TestSecurityConfig;
import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        channelRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();
        testUser = userService.createUser(userRequest);

        // Create test channel
        testChannel = Channel.builder()
            .id(UUID.randomUUID())
            .name("test-channel")
            .type(ChannelType.PUBLIC)
            .build();
        testChannel.getMembers().add(testUser);
        testChannel = channelRepository.save(testChannel);
    }

    @Test
    void createChannel_Success() {
        String channelName = "new-channel";
        Channel channel = chatService.createChannel(channelName, ChannelType.PUBLIC, testUser.getId());
        
        assertNotNull(channel);
        assertNotNull(channel.getId());
        assertEquals(channelName, channel.getName());
        assertEquals(ChannelType.PUBLIC, channel.getType());
        assertTrue(channel.getMembers().contains(testUser));
    }

    @Test
    void addMemberToChannel_Success() {
        // Create a new user to add to the channel
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .username("newuser")
            .email("new@example.com")
            .password("password123")
            .build();
        User newUser = userService.createUser(userRequest);

        // Add the new user to the channel
        chatService.addMemberToChannel(testChannel.getId(), newUser.getId());

        // Verify the user was added
        Channel updatedChannel = channelRepository.findById(testChannel.getId()).orElseThrow();
        assertTrue(updatedChannel.getMembers().contains(newUser));
    }

    @Test
    void removeMemberFromChannel_Success() {
        // Create a new user to add and then remove
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .username("memberuser")
            .email("member@example.com")
            .password("password123")
            .build();
        User member = userService.createUser(userRequest);

        // Add and then remove the member
        chatService.addMemberToChannel(testChannel.getId(), member.getId());
        chatService.removeMemberFromChannel(testChannel.getId(), member.getId());

        // Verify the member was removed
        Channel updatedChannel = channelRepository.findById(testChannel.getId()).orElseThrow();
        assertFalse(updatedChannel.getMembers().contains(member));
    }

    @Test
    void getUserChannels_Success() {
        // Create a new user and add them to the test channel
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .username("memberuser")
            .email("member@example.com")
            .password("password123")
            .build();
        User member = userService.createUser(userRequest);

        chatService.addMemberToChannel(testChannel.getId(), member.getId());
        
        // Get the user's channels
        var channels = chatService.getUserChannels(member.getId());
        
        // Verify the results
        assertNotNull(channels);
        assertEquals(1, channels.size());
        assertEquals(testChannel.getId(), channels.get(0).getId());
    }
} 