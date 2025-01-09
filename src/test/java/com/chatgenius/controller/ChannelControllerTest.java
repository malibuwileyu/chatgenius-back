package com.chatgenius.controller;

import com.chatgenius.config.TestSecurityConfig;
import com.chatgenius.dto.request.CreateChannelRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.service.ChannelService;
import com.chatgenius.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private UserService userService;

    private UUID userId;
    private UUID channelId;
    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        channelId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setCreatedAt(ZonedDateTime.now());

        testChannel = new Channel();
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.setId(channelId);
        testChannel.setName("test-channel");
        testChannel.setCreatedAt(ZonedDateTime.now());
        testChannel.getMembers().add(testUser);

        when(channelService.createChannel(any(CreateChannelRequest.class))).thenReturn(testChannel);
        doNothing().when(channelService).addMember(any(UUID.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createChannel_Success() throws Exception {
        CreateChannelRequest request = CreateChannelRequest.builder()
            .name("test-channel")
            .type(ChannelType.PUBLIC)
            .build();

        mockMvc.perform(post("/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("test-channel"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void addMember_Success() throws Exception {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");

        mockMvc.perform(post("/channels/{channelId}/members/{userId}", channelId, newUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(channelService).addMember(channelId, newUser.getId());
    }
} 