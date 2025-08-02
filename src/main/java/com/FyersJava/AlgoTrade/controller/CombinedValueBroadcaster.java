package com.FyersJava.AlgoTrade.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class CombinedValueBroadcaster {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastCombinedValue(double combinedValue) {
        messagingTemplate.convertAndSend("/topic/combined-value", combinedValue);
    }
}
