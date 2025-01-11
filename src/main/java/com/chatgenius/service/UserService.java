package com.chatgenius.service;

import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();
    User getUser(UUID id);
    User createUser(CreateUserRequest request);
    void deleteUser(UUID id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    User findByUsername(String username);
    
    // Presence-related methods
    List<User> getOnlineUsers(UUID channelId);
    String getUserStatus(UUID userId);
    void updateUserStatus(UUID userId, String status);
} 