package com.chatgenius.repository;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        channelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createChannel_Success() {
        Channel channel = new Channel();
        channel.setName("test-channel");
        channel.setType(ChannelType.PUBLIC);
        channel.setCreatedAt(ZonedDateTime.now());

        Channel savedChannel = channelRepository.save(channel);

        assertNotNull(savedChannel.getId());
        assertEquals("test-channel", savedChannel.getName());
        assertEquals(ChannelType.PUBLIC, savedChannel.getType());
    }

    @Test
    void findByType_Success() {
        Channel channel1 = new Channel();
        channel1.setName("public-channel");
        channel1.setType(ChannelType.PUBLIC);
        channel1.setCreatedAt(ZonedDateTime.now());
        channelRepository.save(channel1);

        Channel channel2 = new Channel();
        channel2.setName("private-channel");
        channel2.setType(ChannelType.PRIVATE);
        channel2.setCreatedAt(ZonedDateTime.now());
        channelRepository.save(channel2);

        List<Channel> publicChannels = channelRepository.findByType(ChannelType.PUBLIC);
        assertEquals(1, publicChannels.size());
        assertEquals("public-channel", publicChannels.get(0).getName());
    }
} 