package com.chatgenius.controller;

import com.chatgenius.config.TestSecurityConfig;
import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.service.ChatService;
import com.chatgenius.service.MessageService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserService userService;

    private Message testMessage;
    private Channel testChannel;
    private User testUser;
    private UUID messageId;
    private UUID channelId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        messageId = UUID.randomUUID();
        channelId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(ZonedDateTime.now());

        testChannel = new Channel();
        testChannel.setId(channelId);
        testChannel.setName("testchannel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.setCreatedAt(ZonedDateTime.now());
        testChannel.getMembers().add(testUser);

        testMessage = new Message();
        testMessage.setId(messageId);
        testMessage.setContent("Test message");
        testMessage.setType(MessageType.TEXT);
        testMessage.setChannel(testChannel);
        testMessage.setUser(testUser);
        testMessage.setCreatedAt(ZonedDateTime.now());

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(messageService.getChannelMessages(eq(channelId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testMessage)));
        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(testMessage);
        when(messageService.getMessage(messageId)).thenReturn(testMessage);
        when(messageService.updateMessage(eq(messageId), anyString())).thenReturn(testMessage);
        doNothing().when(messageService).deleteMessage(messageId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createMessage_Success() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
            .content("Test message")
            .type(MessageType.TEXT)
            .channelId(channelId)
            .userId(userId)
            .build();

        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getChannelMessages_Success() throws Exception {
        mockMvc.perform(get("/messages/channel/{channelId}", channelId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].content").value("Test message"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getMessage_Success() throws Exception {
        mockMvc.perform(get("/messages/{id}", messageId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateMessage_Success() throws Exception {
        mockMvc.perform(put("/messages/{id}", messageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("Updated message"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteMessage_Success() throws Exception {
        mockMvc.perform(delete("/messages/{id}", messageId))
            .andExpect(status().isOk());
    }
} 