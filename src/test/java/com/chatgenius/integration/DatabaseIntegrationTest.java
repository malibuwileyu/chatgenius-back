package com.chatgenius.integration;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DatabaseIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void testMessageThreading() {
        // Create user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());
        entityManager.persist(user);

        // Create channel
        Channel channel = new Channel();
        channel.setName("test-channel");
        channel.setType(ChannelType.PUBLIC);
        channel.setCreatedAt(ZonedDateTime.now());
        channel.setMembers(new HashSet<>());
        channel.getMembers().add(user);
        entityManager.persist(channel);

        // Create thread starter message
        Message threadStarter = new Message();
        threadStarter.setContent("Thread starter");
        threadStarter.setType(MessageType.THREAD_START);
        threadStarter.setUser(user);
        threadStarter.setChannel(channel);
        threadStarter.setCreatedAt(ZonedDateTime.now());
        entityManager.persist(threadStarter);

        // Create reply
        Message reply = new Message();
        reply.setContent("Thread reply");
        reply.setType(MessageType.THREAD_REPLY);
        reply.setUser(user);
        reply.setChannel(channel);
        reply.setThreadId(threadStarter.getId());
        reply.setCreatedAt(ZonedDateTime.now());
        entityManager.persist(reply);

        entityManager.flush();

        // Test thread retrieval
        List<Message> threadMessages = messageRepository.findByThreadId(threadStarter.getId());
        assertEquals(1, threadMessages.size());
        Message savedReply = threadMessages.get(0);
        assertEquals("Thread reply", savedReply.getContent());
        assertEquals(threadStarter.getId(), savedReply.getThreadId());
        assertEquals(MessageType.THREAD_REPLY, savedReply.getType());
    }
} 