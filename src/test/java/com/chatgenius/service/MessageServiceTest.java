package com.chatgenius.service;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User testUser;
    private Channel testChannel;
    private Message testMessage;
    private UUID userId;
    private UUID channelId;
    private UUID messageId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("05680472-9789-4cd6-8e70-7eebcb9af00f");
        channelId = UUID.fromString("de23276a-e112-4554-92c8-3fe875aad09c");
        messageId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testChannel = new Channel();
        testChannel.setId(channelId);
        testChannel.setName("test-channel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.getMembers().add(testUser);

        testMessage = new Message();
        testMessage.setId(messageId);
        testMessage.setContent("Test message");
        testMessage.setType(MessageType.TEXT);
        testMessage.setUser(testUser);
        testMessage.setChannel(testChannel);
        testMessage.setCreatedAt(ZonedDateTime.now());

        // Set up common mock behavior
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(testChannel));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createMessage_Success() {
        CreateMessageRequest request = CreateMessageRequest.builder()
            .content("Test message")
            .type(MessageType.TEXT)
            .channelId(channelId)
            .userId(userId)
            .build();

        Message message = messageService.createMessage(request);
        assertNotNull(message);
        assertEquals("Test message", message.getContent());
        assertEquals(MessageType.TEXT, message.getType());
        assertEquals(userId, message.getUser().getId());
        assertEquals(channelId, message.getChannel().getId());

        verify(channelRepository).findById(channelId);
        verify(userRepository).findById(userId);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getChannelMessages_Success() {
        when(messageRepository.findByChannelId(eq(channelId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testMessage)));

        Page<Message> messages = messageService.getChannelMessages(channelId, Pageable.unpaged());
        assertEquals(1, messages.getTotalElements());
        assertEquals("Test message", messages.getContent().get(0).getContent());

        verify(channelRepository).findById(channelId);
        verify(messageRepository).findByChannelId(eq(channelId), any(Pageable.class));
    }

    @Test
    void getMessage_Success() {
        Message message = messageService.getMessage(messageId);
        assertNotNull(message);
        assertEquals(messageId, message.getId());
        assertEquals("Test message", message.getContent());

        verify(messageRepository).findById(messageId);
    }

    @Test
    void updateMessage_Success() {
        Message message = messageService.updateMessage(messageId, "Updated message");
        assertNotNull(message);
        assertEquals("Updated message", message.getContent());

        verify(messageRepository).findById(messageId);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void deleteMessage_Success() {
        doNothing().when(messageRepository).delete(testMessage);

        messageService.deleteMessage(messageId);
        
        verify(messageRepository).findById(messageId);
        verify(messageRepository).delete(testMessage);
    }
} 