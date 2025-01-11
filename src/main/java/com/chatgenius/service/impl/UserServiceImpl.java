package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.exception.ValidationException;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Map<UUID, String> userStatuses = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> channelUsers = new ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ValidationException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setStatus(UserStatus.OFFLINE);
        user.setCreatedAt(ZonedDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public List<User> getOnlineUsers(UUID channelId) {
        Set<UUID> channelUserIds = channelUsers.getOrDefault(channelId, Collections.emptySet());
        return channelUserIds.stream()
            .filter(userId -> "online".equals(userStatuses.get(userId)))
            .map(this::getUser)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public String getUserStatus(UUID userId) {
        return userStatuses.getOrDefault(userId, "offline");
    }

    @Override
    public void updateUserStatus(UUID userId, String status) {
        userStatuses.put(userId, status);
    }
} 