package com.chatgenius.config;

import com.chatgenius.model.Channel;
import com.chatgenius.model.Message;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.model.enums.MessageType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String TEST_USERNAME = "testuser";
    private static final UUID TEST_USER_UUID = UUID.fromString("c144a9ba-7885-4a71-ba2c-90988a0a94f3");
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already initialized");
            return;
        }

        log.info("Starting data initialization...");

        // Create users
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password" + i);
            users.add(userRepository.save(user));
        }
        log.info("Created {} users", users.size());

        // Create channels
        List<Channel> channels = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Channel channel = new Channel();
            channel.setName("channel" + i);
            channel.setType(ChannelType.PUBLIC);
            channel.setCreatedAt(ZonedDateTime.now());
            channel.setMembers(new HashSet<>());
            
            // Add some users to each channel
            for (int j = 0; j < 3; j++) {
                channel.getMembers().add(users.get((i + j) % users.size()));
            }
            
            channels.add(channelRepository.save(channel));
        }
        log.info("Created {} channels", channels.size());

        // Create messages
        channels.forEach(channel -> {
            channel.getMembers().forEach(user -> {
                // Create a regular message
                Message message = new Message();
                message.setContent("Message from " + user.getUsername() + " in " + channel.getName());
                message.setType(MessageType.TEXT);
                message.setUser(user);
                message.setChannel(channel);
                message.setCreatedAt(ZonedDateTime.now());
                messageRepository.save(message);

                // Create a thread starter message
                Message threadStarter = new Message();
                threadStarter.setContent("Thread starter from " + user.getUsername());
                threadStarter.setType(MessageType.THREAD_START);
                threadStarter.setUser(user);
                threadStarter.setChannel(channel);
                threadStarter.setCreatedAt(ZonedDateTime.now());
                threadStarter = messageRepository.save(threadStarter);

                // Create thread replies
                Message reply = new Message();
                reply.setContent("Reply to thread from " + user.getUsername());
                reply.setType(MessageType.THREAD_REPLY);
                reply.setUser(user);
                reply.setChannel(channel);
                reply.setThreadId(threadStarter.getId());
                reply.setCreatedAt(ZonedDateTime.now());
                messageRepository.save(reply);
            });
        });
        log.info("Created messages for all channels");

        log.info("Data initialization completed");
    }
} 