package com.chatgenius.service;

import com.chatgenius.model.Reaction;

import java.util.List;
import java.util.UUID;

public interface ReactionService {
    /**
     * Add a reaction to a message
     * @param messageId The ID of the message
     * @param userId The ID of the user adding the reaction
     * @param emoji The emoji to add
     * @return The created reaction
     */
    Reaction addReaction(UUID messageId, UUID userId, String emoji);

    /**
     * Remove a reaction from a message
     * @param messageId The ID of the message
     * @param userId The ID of the user removing the reaction
     * @param emoji The emoji to remove
     */
    void removeReaction(UUID messageId, UUID userId, String emoji);

    /**
     * Get all reactions for a message
     * @param messageId The ID of the message
     * @return List of reactions
     */
    List<Reaction> getMessageReactions(UUID messageId);

    /**
     * Get reaction counts by emoji for a message
     * @param messageId The ID of the message
     * @return Map of emoji to count
     */
    List<ReactionCount> getReactionCounts(UUID messageId);

    /**
     * Record class for reaction counts
     */
    record ReactionCount(String emoji, long count) {}
} 