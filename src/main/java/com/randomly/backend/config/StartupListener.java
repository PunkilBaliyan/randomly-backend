package com.randomly.backend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupListener {

    private final Environment env;

    public StartupListener(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  RANDOMLY BACKEND IS READY               ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  Server Port: " + String.format("%-26s", port) + "║");
        System.out.println("║  Context Path: " + String.format("%-23s", contextPath) + "║");
        System.out.println("║  WebSocket: ws://localhost:" + port + "/ws" + " ".repeat(7) + "║");
        System.out.println("║  Health: http://localhost:" + port + "/health" + " ".repeat(9) + "║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("\n");
    }
}
