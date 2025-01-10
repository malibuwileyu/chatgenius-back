package com.chatgenius.controller;

import com.chatgenius.dto.request.LoginRequest;
import com.chatgenius.dto.request.RegisterRequest;
import com.chatgenius.dto.response.ErrorResponse;
import com.chatgenius.dto.response.JwtResponse;
import com.chatgenius.dto.response.MessageResponse;
import com.chatgenius.dto.response.SimpleMessageResponse;
import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import com.chatgenius.service.TokenBlacklistService;
import com.chatgenius.service.UserService;
import com.chatgenius.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Set;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder,
                         TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("\n=== Login Attempt ===");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("Time: " + ZonedDateTime.now());
        
        try {
            System.out.println("Attempting authentication...");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            System.out.println("Authentication successful");

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("User authorities: " + userDetails.getAuthorities());
            
            String jwt = jwtUtil.generateToken(userDetails);
            System.out.println("JWT token generated successfully");
            System.out.println("Token length: " + jwt.length());
            System.out.println("=== Login Complete ===\n");

            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (BadCredentialsException e) {
            System.out.println("Login failed: Invalid credentials");
            System.out.println("=== Login Failed ===\n");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password"));
        } catch (Exception e) {
            System.out.println("Login failed: Unexpected error");
            System.out.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            System.out.println("=== Login Error ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred during authentication"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("\n=== Registration Attempt ===");
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Time: " + ZonedDateTime.now());
        
        try {
            if (userService.existsByUsername(request.getUsername())) {
                System.out.println("Registration failed: Username already exists");
                System.out.println("=== Registration Failed ===\n");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Username is already taken"));
            }

            if (userService.existsByEmail(request.getEmail())) {
                System.out.println("Registration failed: Email already exists");
                System.out.println("=== Registration Failed ===\n");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email is already in use"));
            }

            System.out.println("Creating new user...");
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(Set.of("ROLE_USER"));
            user.setStatus(UserStatus.OFFLINE);
            user.setCreatedAt(ZonedDateTime.now());

            userService.save(user);
            System.out.println("User created successfully");
            System.out.println("=== Registration Complete ===\n");

            return ResponseEntity.ok(new SimpleMessageResponse("User registered successfully"));
        } catch (Exception e) {
            System.out.println("Registration failed: Unexpected error");
            System.out.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            System.out.println("=== Registration Error ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred during registration"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        System.out.println("\n=== Logout Attempt ===");
        System.out.println("Time: " + ZonedDateTime.now());
        
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                System.out.println("Token received and parsed");
                
                // Get remaining expiration time from the token
                long expirationTime = jwtUtil.getExpirationTimeFromToken(jwt);
                System.out.println("Token expiration time remaining: " + expirationTime + "ms");
                
                // Add token to blacklist with its remaining time to live
                tokenBlacklistService.blacklistToken(jwt, expirationTime);
                System.out.println("Token blacklisted successfully");
                
                SecurityContextHolder.clearContext();
                System.out.println("Security context cleared");
                System.out.println("=== Logout Complete ===\n");
                
                return ResponseEntity.ok(new SimpleMessageResponse("Logged out successfully"));
            }
            System.out.println("Logout failed: Invalid token format");
            System.out.println("=== Logout Failed ===\n");
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid token format"));
        } catch (Exception e) {
            System.out.println("Logout failed: Unexpected error");
            System.out.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            System.out.println("=== Logout Error ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred during logout"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        System.out.println("\n=== Token Refresh Attempt ===");
        System.out.println("Time: " + ZonedDateTime.now());
        System.out.println("Authorization header: " + token);
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Refresh failed: Invalid token format");
                System.out.println("=== Token Refresh Failed ===\n");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid token format - Bearer token required"));
            }

            String oldToken = token.substring(7);
            System.out.println("Old token received and parsed, length: " + oldToken.length());
            
            try {
                // First try to extract username to catch malformed tokens early
                String username = jwtUtil.extractUsername(oldToken);
                System.out.println("Username extracted from token: " + username);
                
                // Then check blacklist
                if (tokenBlacklistService.isTokenBlacklisted(oldToken)) {
                    System.out.println("Refresh failed: Token is blacklisted");
                    System.out.println("=== Token Refresh Failed ===\n");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token is blacklisted"));
                }
                System.out.println("Token blacklist check passed");
                
                UserDetails userDetails = userService.loadUserByUsername(username);
                System.out.println("User details loaded: " + userDetails.getUsername());
                System.out.println("User authorities: " + userDetails.getAuthorities());

                if (jwtUtil.validateToken(oldToken, userDetails)) {
                    System.out.println("Old token validated successfully");
                    
                    // Generate new token before blacklisting old one
                    String newToken = jwtUtil.generateToken(userDetails);
                    System.out.println("New token generated, length: " + newToken.length());
                    
                    // Blacklist the old token
                    long oldTokenExpiration = jwtUtil.getExpirationTimeFromToken(oldToken);
                    tokenBlacklistService.blacklistToken(oldToken, oldTokenExpiration);
                    System.out.println("Old token blacklisted");
                    
                    System.out.println("=== Token Refresh Complete ===\n");
                    return ResponseEntity.ok(new JwtResponse(newToken));
                } else {
                    System.out.println("Refresh failed: Token validation failed");
                    System.out.println("=== Token Refresh Failed ===\n");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token validation failed"));
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Refresh failed: Token has expired");
                System.out.println("=== Token Refresh Failed ===\n");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token has expired"));
            }
        } catch (Exception e) {
            System.out.println("Token refresh failed: Unexpected error");
            System.out.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            System.out.println("=== Token Refresh Error ===\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred while refreshing token"));
        }
    }
} 