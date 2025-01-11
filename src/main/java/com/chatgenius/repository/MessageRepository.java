package com.chatgenius.repository;

import com.chatgenius.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC")
    List<Message> findByChannelId(@Param("channelId") UUID channelId);

    @Query("SELECT m FROM Message m WHERE m.threadId = :threadId ORDER BY m.createdAt ASC")
    List<Message> findByThreadId(@Param("threadId") UUID threadId);

    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.type = 'THREAD_START' ORDER BY m.createdAt DESC")
    List<Message> findThreadStarters(@Param("channelId") UUID channelId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.channel.id = :channelId")
    void deleteByChannelId(@Param("channelId") UUID channelId);
} 