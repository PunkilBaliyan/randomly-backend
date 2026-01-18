package com.randomly.backend.ws.dto;

import java.time.Instant;

public record ChatMessage(
        String sessionId,
        String fromUserId,
        String text,
        Instant timestamp
) {}
