package com.chatgenius.repository;

import com.chatgenius.model.Channel;
import com.chatgenius.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    
    // Find channels by member ID
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m.id = :memberId")
    List<Channel> findByMemberId(@Param("memberId") UUID memberId);
    
    // Find all public channels
    @Query("SELECT c FROM Channel c WHERE c.type = 'PUBLIC'")
    List<Channel> findPublicChannels();
    
    // Find direct message channels
    @Query("SELECT c FROM Channel c WHERE c.type = 'DIRECT_MESSAGE' AND EXISTS (SELECT m FROM c.members m WHERE m.id = :userId)")
    List<Channel> findDirectMessageChannels(@Param("userId") UUID userId);
    
    // Additional useful queries
    boolean existsByName(String name);
    List<Channel> findByType(ChannelType type);
} 