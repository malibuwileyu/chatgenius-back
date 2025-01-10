package com.chatgenius.service;

import java.util.List;
import java.util.UUID;

public interface PresenceService {
    /**
     * Updates a user's presence status
     * @param userId The ID of the user
     * @param status The new status (e.g., "online", "offline", "away")
     */
    void updateUserPresence(UUID userId, String status);

    /**
     * Gets a list of online users
     * @return List of user IDs that are currently online
     */
    List<UUID> getOnlineUsers();

    /**
     * Handles user disconnection
     * @param userId The ID of the user who disconnected
     */
    void handleDisconnect(UUID userId);

    /**
     * Gets a user's current presence status
     * @param userId The ID of the user
     * @return The user's current status
     */
    String getUserStatus(UUID userId);
} 