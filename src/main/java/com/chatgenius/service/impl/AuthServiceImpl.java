package com.chatgenius.service.impl;

import com.chatgenius.dto.request.LoginRequest;
import com.chatgenius.dto.request.RegisterRequest;
import com.chatgenius.dto.response.AuthResponse;
import com.chatgenius.dto.response.UserResponse;
import com.chatgenius.model.User;
import com.chatgenius.service.AuthService;
import com.chatgenius.service.UserService;
import com.chatgenius.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .user(UserResponse.fromUser(user))
            .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        user = userService.createUser(user);
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .user(UserResponse.fromUser(user))
            .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userService.findByUsername(username);
        
        if (jwtUtil.validateToken(refreshToken, user)) {
            String newToken = jwtUtil.generateToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            
            return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(UserResponse.fromUser(user))
                .build();
        }
        
        throw new IllegalArgumentException("Invalid refresh token");
    }

    @Override
    public void logout(String token) {
        // Implement token blacklisting or invalidation if needed
    }
} 