package com.randomly.backend.ws.dto;

public record MatchResponse(
        String sessionId,
        boolean isCaller
) {}
