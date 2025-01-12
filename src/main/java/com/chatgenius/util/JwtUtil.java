package com.chatgenius.util;

import com.chatgenius.model.User;
import com.chatgenius.repository.TokenBlacklistRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    public JwtUtil(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new RuntimeException("Token parsing error", e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        if (userDetails instanceof User) {
            claims.put("id", ((User) userDetails).getId().toString());
        }
        
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User) {
            claims.put("id", ((User) userDetails).getId().toString());
        }
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        if (expirationTime <= 0) {
            throw new IllegalArgumentException("Token expiration time must be positive");
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Try to check blacklist, but don't fail validation if Redis is down
            try {
                if (tokenBlacklistRepository.isTokenBlacklisted(token)) {
                    System.out.println("JWT Validation - Token is blacklisted");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("JWT Validation - Warning: Unable to check token blacklist: " + e.getMessage());
                // Continue with validation even if Redis is down
            }

            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();
            
            System.out.println("JWT Validation - Token details:");
            System.out.println("  Username from token: " + username);
            System.out.println("  Username from userDetails: " + userDetails.getUsername());
            System.out.println("  Issued at: " + issuedAt);
            System.out.println("  Expires at: " + expiration);
            System.out.println("  Current time: " + new Date());
            
            if (expiration != null && expiration.before(new Date())) {
                System.out.println("JWT Validation - Token has expired");
                throw new ExpiredJwtException(null, claims, "Token has expired");
            }
            
            boolean isValid = username.equals(userDetails.getUsername());
            System.out.println("JWT Validation - Username match: " + isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Validation - Token expired exception: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("JWT Validation - Validation error: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setAllowedClockSkewSeconds(0)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Token parsing error", e);
        }
    }

    public void invalidateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            long expirationTimeInMillis = expiration.getTime() - System.currentTimeMillis();
            if (expirationTimeInMillis > 0) {
                tokenBlacklistRepository.blacklistToken(token, expirationTimeInMillis);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error invalidating token", e);
        }
    }

    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.isTokenBlacklisted(token);
    }

    public long getExpirationTimeFromToken(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
} 