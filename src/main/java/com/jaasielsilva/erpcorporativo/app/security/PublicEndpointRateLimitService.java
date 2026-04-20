package com.jaasielsilva.erpcorporativo.app.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PublicEndpointRateLimitService {

    private static final int MAX_START_ATTEMPTS = 8;
    private static final long START_ATTEMPT_WINDOW_MS = Duration.ofMinutes(10).toMillis();
    private static final long BLOCK_TIME_MS = Duration.ofMinutes(15).toMillis();

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String clientKey) {
        AttemptState state = attempts.get(clientKey);
        if (state == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (state.blockedUntil() > now) {
            return true;
        }
        if (state.firstAttemptAt() + START_ATTEMPT_WINDOW_MS < now) {
            attempts.remove(clientKey);
            return false;
        }
        return false;
    }

    public void registerAttempt(String clientKey) {
        long now = System.currentTimeMillis();
        attempts.compute(clientKey, (key, current) -> {
            if (current == null || current.firstAttemptAt() + START_ATTEMPT_WINDOW_MS < now) {
                return new AttemptState(1, now, 0L);
            }

            int nextCount = current.count() + 1;
            long blockedUntil = nextCount >= MAX_START_ATTEMPTS ? now + BLOCK_TIME_MS : current.blockedUntil();
            return new AttemptState(nextCount, current.firstAttemptAt(), blockedUntil);
        });
    }

    private record AttemptState(int count, long firstAttemptAt, long blockedUntil) {
    }
}
