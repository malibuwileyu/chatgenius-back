package com.chatgenius.controller;

import com.chatgenius.config.TestConfig;
import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private Message testMessage;
    private CreateMessageRequest createRequest;
    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        testChannel = new Channel();
        testChannel.setId(UUID.randomUUID());
        testChannel.setName("testchannel");

        testMessage = new Message();
        testMessage.setId(UUID.randomUUID());
        testMessage.setContent("Test message");
        testMessage.setType(MessageType.TEXT);
        testMessage.setUser(testUser);
        testMessage.setChannel(testChannel);
        testMessage.setCreatedAt(ZonedDateTime.now());

        createRequest = CreateMessageRequest.builder()
            .content("Test message")
            .channelId(testChannel.getId())
            .userId(testUser.getId())
            .type(MessageType.TEXT)
            .build();
    }

    @Test
    @WithMockUser
    void getMessages_Success() throws Exception {
        Page<Message> messagePage = new PageImpl<>(Collections.singletonList(testMessage));
        when(messageService.getChannelMessages(any(UUID.class), any(Pageable.class)))
            .thenReturn(messagePage);

        mockMvc.perform(get("/api/messages")
                .param("channelId", testChannel.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value(testMessage.getContent()))
                .andExpect(jsonPath("$.content[0].type").value(testMessage.getType().toString()));
    }

    @Test
    @WithMockUser
    void createMessage_Success() throws Exception {
        when(messageService.createMessage(any(CreateMessageRequest.class)))
            .thenReturn(testMessage);

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(testMessage.getContent()))
                .andExpect(jsonPath("$.type").value(testMessage.getType().toString()));
    }

    @Test
    @WithMockUser
    void getMessage_Success() throws Exception {
        when(messageService.getMessage(any(UUID.class)))
            .thenReturn(testMessage);

        mockMvc.perform(get("/api/messages/{id}", testMessage.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(testMessage.getContent()))
                .andExpect(jsonPath("$.type").value(testMessage.getType().toString()));
    }

    @Test
    @WithMockUser
    void updateMessage_Success() throws Exception {
        String newContent = "Updated content";
        testMessage.setContent(newContent);
        
        when(messageService.updateMessage(any(UUID.class), any(String.class)))
            .thenReturn(testMessage);

        mockMvc.perform(put("/api/messages/{id}", testMessage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(newContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(newContent));
    }

    @Test
    @WithMockUser
    void deleteMessage_Success() throws Exception {
        mockMvc.perform(delete("/api/messages/{id}", testMessage.getId()))
                .andExpect(status().isOk());
    }
} 