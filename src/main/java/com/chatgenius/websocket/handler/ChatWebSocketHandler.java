package com.chatgenius.websocket.handler;

import com.chatgenius.model.Channel;
import com.chatgenius.model.User;
import com.chatgenius.service.ChannelService;
import com.chatgenius.service.UserService;
import com.chatgenius.websocket.event.WebSocketEvents.ChannelEvent;
import com.chatgenius.websocket.event.WebSocketEvents.ErrorEvent;
import com.chatgenius.websocket.event.WebSocketEvents.TypingEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelService channelService;
    private final UserService userService;

    @MessageMapping("/channel.join.{channelId}")
    public void handleChannelJoin(@DestinationVariable UUID channelId,
                                @Payload UUID userId,
                                SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Handling channel join request for channel: {} by user: {}", channelId, userId);
        
        try {
            channelService.addMember(channelId, userId);
            
            // Get updated channel details
            Channel channel = channelService.getChannel(channelId);
            User user = userService.getUser(userId);
            
            // Notify channel members about the new join
            messagingTemplate.convertAndSend(
                "/topic/channel." + channelId,
                new ChannelEvent("channel:joined", channel, user)
            );
            
            // Add channel to user's subscriptions
            headerAccessor.getSessionAttributes().put("channel_" + channelId, true);
            
            logger.info("User {} successfully joined channel {}", userId, channelId);
        } catch (Exception e) {
            logger.error("Error handling channel join", e);
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/errors",
                new ErrorEvent("Failed to join channel: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/channel.leave.{channelId}")
    public void handleChannelLeave(@DestinationVariable UUID channelId,
                                 @Payload UUID userId,
                                 SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Handling channel leave request for channel: {} by user: {}", channelId, userId);
        
        try {
            channelService.removeMember(channelId, userId);
            
            // Get updated channel details
            Channel channel = channelService.getChannel(channelId);
            User user = userService.getUser(userId);
            
            // Notify channel members about the leave
            messagingTemplate.convertAndSend(
                "/topic/channel." + channelId,
                new ChannelEvent("channel:left", channel, user)
            );
            
            // Remove channel from user's subscriptions
            headerAccessor.getSessionAttributes().remove("channel_" + channelId);
            
            logger.info("User {} successfully left channel {}", userId, channelId);
        } catch (Exception e) {
            logger.error("Error handling channel leave", e);
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/errors",
                new ErrorEvent("Failed to leave channel: " + e.getMessage())
            );
        }
    }

    @MessageMapping("/channel.typing.{channelId}")
    public void handleTypingIndicator(@DestinationVariable UUID channelId,
                                    @Payload TypingEvent typingEvent) {
        logger.debug("Handling typing indicator for channel: {} by user: {}", 
            channelId, typingEvent.userId());
        
        try {
            // Broadcast typing status to channel members
            messagingTemplate.convertAndSend(
                "/topic/typing." + channelId,
                typingEvent
            );
        } catch (Exception e) {
            logger.error("Error handling typing indicator", e);
            messagingTemplate.convertAndSendToUser(
                typingEvent.userId().toString(),
                "/queue/errors",
                new ErrorEvent("Failed to broadcast typing status: " + e.getMessage())
            );
        }
    }
} 