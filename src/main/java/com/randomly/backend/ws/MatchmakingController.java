package com.randomly.backend.ws;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Handles user matchmaking requests.
 * Routes users to appropriate queues based on company selection.
 */
@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private static final Logger log = LoggerFactory.getLogger(MatchmakingController.class);

    private final MatchingService matchingService;

    /**
     * User joins matchmaking queue
     */
    @MessageMapping("/join")
    public void joinQueue(JoinRequest req) {
        log.info("[MATCHMAKING] User {} joining queue for company {}", req.userId(), req.company());
        try {
            matchingService.joinQueue(req.userId(), req.company());
        } catch (Exception e) {
            log.error("[MATCHMAKING] Error joining queue for user {}", req.userId(), e);
        }
    }

    /**
     * User cancels matchmaking
     */
    @MessageMapping("/cancel")
    public void cancelQueue(JoinRequest req) {
        log.info("[MATCHMAKING] User {} canceling queue for company {}", req.userId(), req.company());
        try {
            matchingService.cancelQueue(req.userId(), req.company());
        } catch (Exception e) {
            log.error("[MATCHMAKING] Error canceling queue for user {}", req.userId(), e);
        }
    }
}