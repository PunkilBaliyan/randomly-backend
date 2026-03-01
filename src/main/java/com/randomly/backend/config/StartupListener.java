package com.randomly.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupListener {

    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);
    private final Environment env;

    public StartupListener(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            String port = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "/");
            
            logger.info("========================================");
            logger.info("RANDOMLY BACKEND IS READY");
            logger.info("Server Port: {}", port);
            logger.info("Context Path: {}", contextPath);
            logger.info("WebSocket: ws://localhost:{}/ws", port);
            logger.info("Health: http://localhost:{}/health", port);
            logger.info("========================================");
        } catch (Exception e) {
            logger.error("Error in StartupListener", e);
        }
    }
}
