package com.randomly.backend.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles heartbeat ping/pong messages.
 * Helps detect disconnections and keep connections alive.
 */
@Controller
public class PingPongController {

    private static final Logger log = LoggerFactory.getLogger(PingPongController.class);

    /**
     * Respond to ping with pong
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String pong(String message) {
        log.debug("[PING/PONG] Received ping, sending pong");
        return "pong";
    }
}
