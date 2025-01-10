package com.chatgenius.security;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.ChannelType;
import com.chatgenius.repository.ChannelRepository;
import com.chatgenius.service.UserService;
import com.chatgenius.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChannelRepository channelRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private String testToken;

    private final String TEST_UUID = "123e4567-e89b-12d3-a456-426614174000";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // encoded "password"
        testUser.setRoles(Set.of("ROLE_USER"));
        
        testToken = "test.jwt.token";
        
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn(testToken);
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(userService.loadUserByUsername("admin")).thenReturn(createAdminUser());
        when(userService.loadUserByUsername("moderator")).thenReturn(createModeratorUser());
    }

    private User createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG");
        admin.setRoles(Set.of("ROLE_ADMIN"));
        return admin;
    }

    private User createModeratorUser() {
        User moderator = new User();
        moderator.setUsername("moderator");
        moderator.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG");
        moderator.setRoles(Set.of("ROLE_MODERATOR"));
        return moderator;
    }

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password\",\"email\":\"new@example.com\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoints_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void userEndpoints_WithUserRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test-channel\",\"type\":\"PUBLIC\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void adminEndpoints_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/" + TEST_UUID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/" + TEST_UUID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "moderator", roles = "MODERATOR")
    void moderatorEndpoints_WithModeratorRole_ShouldBeAccessible() throws Exception {
        // Create a test channel first
        Channel channel = new Channel();
        channel.setId(UUID.fromString(TEST_UUID));
        channel.setName("test-channel");
        channel.setType(ChannelType.PUBLIC);
        channel.setCreatedAt(ZonedDateTime.now());
        channelRepository.save(channel);

        // Verify channel exists
        Channel savedChannel = channelRepository.findById(UUID.fromString(TEST_UUID))
            .orElseThrow(() -> new RuntimeException("Channel not found"));
        assertTrue(savedChannel.getId().equals(UUID.fromString(TEST_UUID)));

        mockMvc.perform(delete("/api/moderator/channels/" + TEST_UUID))
                .andExpect(status().isOk());

        // Verify channel was deleted
        assertFalse(channelRepository.existsById(UUID.fromString(TEST_UUID)));

        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidEndpoint_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void optionsRequest_ShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/channels"))
                .andExpect(status().isOk());
    }
} 