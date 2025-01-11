package com.chatgenius.service;

import com.chatgenius.dto.request.CreateMessageRequest;
import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private UserRepository userRepository;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageService = new MessageServiceImpl(messageRepository, channelRepository, userRepository);
    }

    @Test
    void createMessage_Success() {
        // Arrange
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String content = "Test message";
        
        Channel channel = new Channel();
        channel.setId(channelId);
        channel.setMembers(new HashSet<>());
        
        User user = new User();
        user.setId(userId);
        channel.getMembers().add(user);

        CreateMessageRequest request = new CreateMessageRequest();
        request.setChannelId(channelId);
        request.setUserId(userId);
        request.setContent(content);
        request.setType(MessageType.TEXT);

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent(content);
        message.setType(MessageType.TEXT);
        message.setUser(user);
        message.setChannel(channel);
        message.setCreatedAt(ZonedDateTime.now());

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // Act
        Message result = messageService.createMessage(request);

        // Assert
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(MessageType.TEXT, result.getType());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getChannelMessages_Success() {
        // Arrange
        UUID channelId = UUID.randomUUID();
        List<Message> messages = Arrays.asList(
            createTestMessage("Message 1"),
            createTestMessage("Message 2")
        );
        
        when(channelRepository.existsById(channelId)).thenReturn(true);
        when(messageRepository.findByChannelId(channelId)).thenReturn(messages);

        PageRequest pageable = PageRequest.of(0, 10);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        Page<Message> expectedPage = new PageImpl<>(
            messages.subList(start, end),
            pageable,
            messages.size()
        );

        // Act
        Page<Message> result = messageService.getChannelMessages(channelId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage.getContent().size(), result.getContent().size());
        verify(messageRepository).findByChannelId(channelId);
    }

    @Test
    void getLatestMessages_Success() {
        // Arrange
        UUID channelId = UUID.randomUUID();
        List<Message> messages = Arrays.asList(
            createTestMessage("Message 1"),
            createTestMessage("Message 2"),
            createTestMessage("Message 3")
        );
        
        when(channelRepository.existsById(channelId)).thenReturn(true);
        when(messageRepository.findByChannelId(channelId)).thenReturn(messages);

        // Act
        List<Message> result = messageService.getLatestMessages(channelId, 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository).findByChannelId(channelId);
    }

    @Test
    void getThreadReplies_Success() {
        // Arrange
        UUID threadId = UUID.randomUUID();
        List<Message> replies = Arrays.asList(
            createTestMessage("Reply 1"),
            createTestMessage("Reply 2")
        );
        
        when(messageRepository.existsById(threadId)).thenReturn(true);
        when(messageRepository.findByThreadId(threadId)).thenReturn(replies);

        // Act
        List<Message> result = messageService.getThreadReplies(threadId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository).findByThreadId(threadId);
    }

    private Message createTestMessage(String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent(content);
        message.setType(MessageType.TEXT);
        message.setCreatedAt(ZonedDateTime.now());
        return message;
    }
} 