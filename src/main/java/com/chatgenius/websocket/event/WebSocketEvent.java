package com.chatgenius.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {
    private String type;
    private Map<String, Object> data;
} 