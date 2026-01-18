package com.randomly.backend.ws.dto;

public record TypingEvent(
        String sessionId,
        String fromUserId,
        boolean typing
) {}
