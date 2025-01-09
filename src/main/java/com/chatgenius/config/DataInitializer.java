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
    private static final UUID TEST_USER_UUID = UUID.fromString("f4f3e8f2-1e36-41c7-b810-2423fe48769a");

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

    private void addTestUserToChannel(Channel channel, User testUser, ChannelRepository channelRepository) {
        if (!channel.getMembers().contains(testUser)) {
            channel.getMembers().add(testUser);
            channelRepository.save(channel);
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
                channelRepository.save(generalChannel);
                addTestUserToChannel(generalChannel, testUser, channelRepository);

                Channel announcements = new Channel();
                announcements.setName("announcements");
                announcements.setType(ChannelType.PUBLIC);
                announcements.setCreatedAt(ZonedDateTime.now());
                channelRepository.save(announcements);
                addTestUserToChannel(announcements, testUser, channelRepository);
            } else {
                // Add test user to all existing channels
                channelRepository.findAll().forEach(channel -> 
                    addTestUserToChannel(channel, testUser, channelRepository)
                );
            }
        };
    }
} 