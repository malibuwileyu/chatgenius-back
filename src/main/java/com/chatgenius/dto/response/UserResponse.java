package com.chatgenius.dto.response;

import com.chatgenius.model.User;
import com.chatgenius.model.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private UserStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastSeenAt;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastSeenAt(user.getLastSeenAt())
                .build();
    }
} 