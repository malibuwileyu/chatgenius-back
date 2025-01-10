package com.chatgenius.service;

import com.chatgenius.dto.request.LoginRequest;
import com.chatgenius.dto.request.RegisterRequest;
import com.chatgenius.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    JwtResponse register(RegisterRequest request);
} 