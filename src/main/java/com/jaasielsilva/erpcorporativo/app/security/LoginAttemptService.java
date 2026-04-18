package com.jaasielsilva.erpcorporativo.app.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final long BLOCK_WINDOW_MILLIS = Duration.ofMinutes(15).toMillis();

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String clientKey) {
        AttemptState state = attempts.get(clientKey);

        if (state == null) {
            return false;
        }

        if (state.blockedUntil() <= System.currentTimeMillis()) {
            attempts.remove(clientKey);
            return false;
        }

        return true;
    }

    public void loginSucceeded(String clientKey) {
        attempts.remove(clientKey);
    }

    public void loginFailed(String clientKey) {
        long now = System.currentTimeMillis();

        attempts.compute(clientKey, (key, currentState) -> {
            if (currentState == null || currentState.blockedUntil() <= now) {
                return new AttemptState(1, 0L);
            }

            int nextFailures = currentState.failures() + 1;
            long blockedUntil = nextFailures >= MAX_FAILURES ? now + BLOCK_WINDOW_MILLIS : 0L;
            return new AttemptState(nextFailures, blockedUntil);
        });
    }

    private record AttemptState(int failures, long blockedUntil) {
    }
}
