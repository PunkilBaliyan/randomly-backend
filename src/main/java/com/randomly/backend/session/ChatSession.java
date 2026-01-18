package com.randomly.backend.session;

import java.time.Instant;

public record ChatSession(
        String sessionId,
        String userA,
        String userB,
        String company,
        Instant createdAt
) {}
