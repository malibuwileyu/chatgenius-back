package com.chatgenius.dto.response;

import com.chatgenius.model.Channel;
import com.chatgenius.model.enums.ChannelType;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class ChannelResponse {
    private UUID id;
    private String name;
    private ChannelType type;
    private ZonedDateTime createdAt;
    private List<UUID> members;

    public static ChannelResponse fromChannel(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .type(channel.getType())
                .createdAt(channel.getCreatedAt())
                .members(channel.getMembers().stream()
                        .map(member -> member.getId())
                        .collect(Collectors.toList()))
                .build();
    }
} 