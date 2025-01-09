package com.chatgenius.service.impl;

import com.chatgenius.dto.request.CreateUserRequest;
import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.exception.ValidationException;
import com.chatgenius.model.User;
import com.chatgenius.repository.UserRepository;
import com.chatgenius.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(CreateUserRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        if (existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(ZonedDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User createUser(User user) {
        if (existsByUsername(user.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username);
    }

    private boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 