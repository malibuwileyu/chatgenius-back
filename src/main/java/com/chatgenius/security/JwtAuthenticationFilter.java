package com.chatgenius.security;

import com.chatgenius.service.TokenBlacklistService;
import com.chatgenius.service.UserService;
import com.chatgenius.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("JWT Filter - Processing " + method + " request to: " + requestPath);
        System.out.println("JWT Filter - Headers: " + request.getHeaderNames());
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            System.out.println("JWT Filter - Skipping authentication for public endpoint: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT Filter - Token present: " + (jwt != null));
            
            if (jwt != null) {
                try {
                    // Check if token is blacklisted
                    if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                        System.out.println("JWT Filter - Token is blacklisted");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String username = jwtUtil.extractUsername(jwt);
                    System.out.println("JWT Filter - Extracted username: " + username);
                    
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    System.out.println("JWT Filter - Loaded user details: " + userDetails);
                    
                    boolean isValid = jwtUtil.validateToken(jwt, userDetails);
                    System.out.println("JWT Filter - Token validation result: " + isValid);
                    
                    if (isValid) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT Filter - Authentication set for user: " + username);
                    } else {
                        System.out.println("JWT Filter - Token validation failed");
                    }
                } catch (ExpiredJwtException e) {
                    System.out.println("JWT Filter - Token expired: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("JWT Filter - Token validation error: " + e.getClass().getName() + " - " + e.getMessage());
                }
            } else {
                System.out.println("JWT Filter - No token found in request");
            }
        } catch (Exception e) {
            System.out.println("JWT Filter - Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath) {
        String[] publicPaths = {
            "/api/auth/test", "/auth/test",
            "/api/auth/login", "/auth/login",
            "/api/auth/register", "/auth/register",
            "/api/auth/user", "/auth/user"
        };
        
        // Special handling for refresh endpoint
        if (requestPath != null && (requestPath.equals("/api/auth/refresh") || requestPath.equals("/auth/refresh"))) {
            return true;
        }
        
        for (String path : publicPaths) {
            if (requestPath != null && requestPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 