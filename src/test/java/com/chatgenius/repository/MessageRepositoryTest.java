package com.chatgenius.repository;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

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
    void findByChannelId_Success() {
        Message message2 = new Message();
        message2.setContent("Another message");
        message2.setType(MessageType.TEXT);
        message2.setUser(testUser);
        message2.setChannel(testChannel);
        message2.setCreatedAt(ZonedDateTime.now());
        messageRepository.save(message2);

        Page<Message> messages = messageRepository.findByChannelId(testChannel.getId(), PageRequest.of(0, 10));
        assertEquals(2, messages.getTotalElements());
        assertTrue(messages.getContent().stream().anyMatch(m -> m.getContent().equals("Test message")));
        assertTrue(messages.getContent().stream().anyMatch(m -> m.getContent().equals("Another message")));
    }

    @Test
    void findRepliesByThreadId_Success() {
        Message reply1 = new Message();
        reply1.setContent("Reply message");
        reply1.setType(MessageType.TEXT);
        reply1.setUser(testUser);
        reply1.setChannel(testChannel);
        reply1.setThread(testMessage);
        reply1.setCreatedAt(ZonedDateTime.now());
        messageRepository.save(reply1);

        Message reply2 = new Message();
        reply2.setContent("Another reply");
        reply2.setType(MessageType.TEXT);
        reply2.setUser(testUser);
        reply2.setChannel(testChannel);
        reply2.setThread(testMessage);
        reply2.setCreatedAt(ZonedDateTime.now());
        messageRepository.save(reply2);

        List<Message> replies = messageRepository.findRepliesByThreadId(testMessage.getId());
        assertEquals(2, replies.size());
        assertTrue(replies.stream().anyMatch(r -> r.getContent().equals("Reply message")));
        assertTrue(replies.stream().anyMatch(r -> r.getContent().equals("Another reply")));
    }

    @Test
    void searchInChannel_Success() {
        Message message2 = new Message();
        message2.setContent("Unique search term");
        message2.setType(MessageType.TEXT);
        message2.setUser(testUser);
        message2.setChannel(testChannel);
        message2.setCreatedAt(ZonedDateTime.now());
        messageRepository.save(message2);

        List<Message> results = messageRepository.searchInChannel(testChannel.getId(), "unique");
        assertEquals(1, results.size());
        assertEquals("Unique search term", results.get(0).getContent());
    }

    @Test
    void countByChannelId_Success() {
        Message message2 = new Message();
        message2.setContent("Another message");
        message2.setType(MessageType.TEXT);
        message2.setUser(testUser);
        message2.setChannel(testChannel);
        message2.setCreatedAt(ZonedDateTime.now());
        messageRepository.save(message2);

        long count = messageRepository.countByChannelId(testChannel.getId());
        assertEquals(2, count);
    }
} 