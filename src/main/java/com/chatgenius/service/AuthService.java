package com.chatgenius.service;

import com.chatgenius.dto.request.LoginRequest;
import com.chatgenius.dto.request.RegisterRequest;
import com.chatgenius.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String token);
} 