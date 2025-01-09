package com.chatgenius.repository;

import com.chatgenius.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    // Find messages by channel ID with pagination
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC")
    Page<Message> findByChannelId(@Param("channelId") UUID channelId, Pageable pageable);
    
    // Find latest messages in channel with limit
    @Query(value = "SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC", 
           countQuery = "SELECT COUNT(m) FROM Message m WHERE m.channel.id = :channelId")
    Page<Message> findLatestInChannel(@Param("channelId") UUID channelId, Pageable pageable);
    
    // Find replies in a thread
    @Query("SELECT m FROM Message m WHERE m.thread.id = :threadId ORDER BY m.createdAt ASC")
    List<Message> findRepliesByThreadId(@Param("threadId") UUID threadId);
    
    // Additional useful queries
    @Query("SELECT COUNT(m) FROM Message m WHERE m.channel.id = :channelId")
    long countByChannelId(@Param("channelId") UUID channelId);
    
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Message> searchInChannel(@Param("channelId") UUID channelId, @Param("keyword") String keyword);
} 