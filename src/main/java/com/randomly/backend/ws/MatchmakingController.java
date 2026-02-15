package com.randomly.backend.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MatchmakingController {

    private final MatchingService matchingService;

    public MatchmakingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * User joins matchmaking queue
     */
    @MessageMapping("/join")
    public void joinQueue(JoinRequest req) {
        matchingService.joinQueue(req.userId(), req.company());
    }

    /**
     * User cancels matchmaking
     */
    @MessageMapping("/cancel")
    public void cancelQueue(JoinRequest req) {
        matchingService.cancelQueue(req.userId());
    }
}