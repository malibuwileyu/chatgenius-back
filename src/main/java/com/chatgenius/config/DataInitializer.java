package com.chatgenius.config;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Configuration
@Profile("dev")
public class DataInitializer {

    private static final String TEST_USERNAME = "testuser";
    private static final UUID TEST_USER_UUID = UUID.fromString("c144a9ba-7885-4a71-ba2c-90988a0a94f3");

    private User getOrCreateTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.findByUsername(TEST_USERNAME)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setId(TEST_USER_UUID);
                newUser.setUsername(TEST_USERNAME);
                newUser.setEmail("test@example.com");
                newUser.setPassword(passwordEncoder.encode("password"));
                newUser.setStatus(UserStatus.ONLINE);
                newUser.setCreatedAt(ZonedDateTime.now());
                return userRepository.save(newUser);
            });
    }

    @Transactional
    private void addTestUserToChannel(Channel channel, User testUser, ChannelRepository channelRepository) {
        // First fetch the channel with its members eagerly loaded
        Channel managedChannel = channelRepository.findById(channel.getId())
                .map(ch -> {
                    ch.getMembers().size(); // Initialize the collection
                    return ch;
                })
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (!managedChannel.getMembers().contains(testUser)) {
            managedChannel.getMembers().add(testUser);
            channelRepository.save(managedChannel);
        }
    }

    @Bean
    @DependsOn("entityManagerFactory")
    @Transactional
    public CommandLineRunner initData(ChannelRepository channelRepository, 
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            // First, ensure test user exists
            final User testUser = getOrCreateTestUser(userRepository, passwordEncoder);

            // Create test channels if none exist
            if (channelRepository.count() == 0) {
                Channel generalChannel = new Channel();
                generalChannel.setName("general");
                generalChannel.setType(ChannelType.PUBLIC);
                generalChannel.setCreatedAt(ZonedDateTime.now());
                generalChannel.getMembers().add(testUser);
                channelRepository.save(generalChannel);

                Channel announcements = new Channel();
                announcements.setName("announcements");
                announcements.setType(ChannelType.PUBLIC);
                announcements.setCreatedAt(ZonedDateTime.now());
                announcements.getMembers().add(testUser);
                channelRepository.save(announcements);
            } else {
                // Add test user to all existing channels
                channelRepository.findAll().forEach(channel -> {
                    if (!channel.getMembers().contains(testUser)) {
                        channel.getMembers().add(testUser);
                        channelRepository.save(channel);
                    }
                });
            }
        };
    }
} 