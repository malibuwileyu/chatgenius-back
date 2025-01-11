package com.chatgenius.websocket.interceptor;

import com.chatgenius.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        log.info("Processing WebSocket handshake request");
        
        try {
            // Extract JWT token from query parameter or header
            String token = extractToken(request);
            if (token == null) {
                log.warn("No authentication token found in request");
                return false;
            }

            // Extract username and validate token
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                log.warn("Could not extract username from token");
                return false;
            }

            // Check if token is blacklisted
            if (jwtUtil.isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            // Check token expiration
            long expirationTime = jwtUtil.getExpirationTimeFromToken(token);
            if (expirationTime <= 0) {
                log.warn("Token has expired");
                return false;
            }

            // Store user info in attributes for later use
            attributes.put("username", username);
            attributes.put("token", token);
            attributes.put("authenticated", true);
            
            log.info("Authentication successful for user: {}", username);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired", e);
            return false;
        } catch (Exception e) {
            log.error("Authentication failed during handshake", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("Error during handshake", exception);
        }
    }

    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    log.info("Found token in query parameter");
                    return token;
                }
            }
        }

        // Try to get token from Authorization header
        List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            String auth = authorization.get(0);
            if (auth.startsWith("Bearer ")) {
                log.info("Found token in Authorization header");
                return auth.substring(7);
            }
        }

        log.warn("No token found in request");
        return null;
    }
} 