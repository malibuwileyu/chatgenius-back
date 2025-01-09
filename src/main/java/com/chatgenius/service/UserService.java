package com.chatgenius.service;

import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();
    User createUser(CreateUserRequest request);
    User createUser(User user);
    User getUser(UUID id);
    void deleteUser(UUID id);
    User findByUsername(String username);
} 