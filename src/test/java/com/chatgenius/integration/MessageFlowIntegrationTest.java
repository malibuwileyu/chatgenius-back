package com.chatgenius.integration;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.MessageService;
import com.chatgenius.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MessageFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User testUser;
    private Channel testChannel;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        messageRepository.deleteAll();
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
        testChannel = new Channel();
        testChannel.setName("test-channel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.getMembers().add(testUser);
        testChannel = channelRepository.save(testChannel);

        // Create test message
        testMessage = new Message();
        testMessage.setContent("Test message");
        testMessage.setType(MessageType.TEXT);
        testMessage.setUser(testUser);
        testMessage.setChannel(testChannel);
        testMessage.setCreatedAt(ZonedDateTime.now());
        testMessage = messageRepository.save(testMessage);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createMessage_Success() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
            .content("New test message")
            .type(MessageType.TEXT)
            .channelId(testChannel.getId())
            .userId(testUser.getId())
            .build();

        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("New test message"));

        assertEquals(2, messageRepository.count());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getChannelMessages_Success() throws Exception {
        mockMvc.perform(get("/messages/channel/{channelId}", testChannel.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].content").value("Test message"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateMessage_Success() throws Exception {
        mockMvc.perform(put("/messages/{id}", testMessage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("Updated message"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Updated message"));

        Message updatedMessage = messageRepository.findById(testMessage.getId()).orElseThrow();
        assertEquals("Updated message", updatedMessage.getContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteMessage_Success() throws Exception {
        mockMvc.perform(delete("/messages/{id}", testMessage.getId()))
            .andExpect(status().isOk());

        assertFalse(messageRepository.existsById(testMessage.getId()));
    }
}
