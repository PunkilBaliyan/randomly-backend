package com.randomly.backend.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RtcSignal(

        @NotBlank
        String sessionId,

        @NotBlank
        String fromUserId,

        @NotNull
        SignalType type,

        @NotNull
        Object payload
) {
    public enum SignalType {
        OFFER,
        ANSWER,
        ICE,
        HANGUP
    }
}
