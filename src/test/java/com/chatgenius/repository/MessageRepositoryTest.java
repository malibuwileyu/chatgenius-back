package com.chatgenius.repository;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Channel channel;
    private User user;

    @BeforeEach
    void setUp() {
        // Create and persist a user
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());
        entityManager.persist(user);

        // Create and persist a channel
        channel = new Channel();
        channel.setName("test-channel");
        channel.setType(ChannelType.PUBLIC);
        channel.setCreatedAt(ZonedDateTime.now());
        channel.setMembers(new HashSet<>());
        channel.getMembers().add(user);
        entityManager.persist(channel);

        entityManager.flush();
    }

    @Test
    void findByChannelId_Success() {
        // Create and persist messages
        Message message1 = createMessage("Message 1", MessageType.TEXT);
        Message message2 = createMessage("Message 2", MessageType.TEXT);
        entityManager.persist(message1);
        entityManager.persist(message2);
        entityManager.flush();

        // Test findByChannelId
        List<Message> messages = messageRepository.findByChannelId(channel.getId());
        
        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertTrue(messages.stream().anyMatch(m -> m.getContent().equals("Message 1")));
        assertTrue(messages.stream().anyMatch(m -> m.getContent().equals("Message 2")));
    }

    @Test
    void findByThreadId_Success() {
        // Create and persist a thread starter message
        Message threadStarter = createMessage("Thread starter", MessageType.THREAD_START);
        entityManager.persist(threadStarter);

        // Create and persist thread replies
        Message reply1 = createMessage("Reply 1", MessageType.THREAD_REPLY);
        reply1.setThreadId(threadStarter.getId());
        entityManager.persist(reply1);

        Message reply2 = createMessage("Reply 2", MessageType.THREAD_REPLY);
        reply2.setThreadId(threadStarter.getId());
        entityManager.persist(reply2);

        entityManager.flush();

        // Test findByThreadId
        List<Message> replies = messageRepository.findByThreadId(threadStarter.getId());
        
        assertNotNull(replies);
        assertEquals(2, replies.size());
        assertTrue(replies.stream().anyMatch(m -> m.getContent().equals("Reply 1")));
        assertTrue(replies.stream().anyMatch(m -> m.getContent().equals("Reply 2")));
    }

    @Test
    void findThreadStarters_Success() {
        // Create and persist thread starter messages
        Message threadStarter1 = createMessage("Thread 1", MessageType.THREAD_START);
        Message threadStarter2 = createMessage("Thread 2", MessageType.THREAD_START);
        Message normalMessage = createMessage("Normal message", MessageType.TEXT);
        
        entityManager.persist(threadStarter1);
        entityManager.persist(threadStarter2);
        entityManager.persist(normalMessage);
        entityManager.flush();

        // Test findThreadStarters
        List<Message> threadStarters = messageRepository.findThreadStarters(channel.getId());
        
        assertNotNull(threadStarters);
        assertEquals(2, threadStarters.size());
        assertTrue(threadStarters.stream().allMatch(m -> m.getType() == MessageType.THREAD_START));
    }

    private Message createMessage(String content, MessageType type) {
        Message message = new Message();
        message.setContent(content);
        message.setType(type);
        message.setUser(user);
        message.setChannel(channel);
        message.setCreatedAt(ZonedDateTime.now());
        return message;
    }
} 