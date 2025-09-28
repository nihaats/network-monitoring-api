package com.network_monitor.controller;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.network_monitor.event.SnmpDataSavedEvent;
import com.network_monitor.model.SnmpData;
import com.network_monitor.model.WebSocketMessage;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final ConcurrentHashMap<String, Boolean> activeSubscriptions = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSnmpDataSaved(SnmpDataSavedEvent event) {
        try {
            List<SnmpData> data = event.getSnmpData();
            String frqType = event.getFrequencyType();
            if (data != null) {
                WebSocketMessage message = new WebSocketMessage(frqType + "_frequency_metrics", data);
                messagingTemplate.convertAndSend("/topic/" + frqType + "-frequency-data", message);
            }
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public WebSocketMessage ping() {
        return new WebSocketMessage("PONG", java.util.Map.of(
                "status", "alive",
                "timestamp", System.currentTimeMillis(),
                "activeSubscriptions", activeSubscriptions.size()));
    }
}