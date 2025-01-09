package com.chatgenius.integration;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DatabaseIntegrationTest {

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
        messageRepository.deleteAll();
        channelRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        testChannel = new Channel();
        testChannel.setName("test-channel");
        testChannel.setType(ChannelType.PUBLIC);
        testChannel.getMembers().add(testUser);
        testChannel = channelRepository.save(testChannel);

        testMessage = new Message();
        testMessage.setContent("Test message");
        testMessage.setType(MessageType.TEXT);
        testMessage.setUser(testUser);
        testMessage.setChannel(testChannel);
        testMessage.setCreatedAt(ZonedDateTime.now());
        testMessage = messageRepository.save(testMessage);
    }

    @Test
    void createMessage_Success() {
        Message message = new Message();
        message.setContent("New message");
        message.setType(MessageType.TEXT);
        message.setUser(testUser);
        message.setChannel(testChannel);
        message.setCreatedAt(ZonedDateTime.now());

        Message savedMessage = messageRepository.save(message);
        assertNotNull(savedMessage.getId());
        assertEquals("New message", savedMessage.getContent());
        assertEquals(testUser.getId(), savedMessage.getUser().getId());
        assertEquals(testChannel.getId(), savedMessage.getChannel().getId());
    }

    @Test
    void createReply_Success() {
        Message reply = new Message();
        reply.setContent("Reply message");
        reply.setType(MessageType.TEXT);
        reply.setUser(testUser);
        reply.setChannel(testChannel);
        reply.setThread(testMessage);
        reply.setCreatedAt(ZonedDateTime.now());

        Message savedReply = messageRepository.save(reply);
        assertNotNull(savedReply.getId());
        assertEquals("Reply message", savedReply.getContent());
        assertEquals(testMessage.getId(), savedReply.getThread().getId());
    }
} 