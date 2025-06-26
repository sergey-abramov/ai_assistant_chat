package com.aiassistant.service.impl;

import com.aiassistant.config.ApplicationProperties;
import com.aiassistant.exception.RateLimitExceededException;
import com.aiassistant.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of RateLimitService.
 * Follows Single Responsibility Principle - handles only rate limiting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryRateLimitService implements RateLimitService {
    
    private final ApplicationProperties properties;
    private final ConcurrentMap<Long, UserRateLimit> rateLimits = new ConcurrentHashMap<>();
    
    @Override
    public boolean canMakeRequest(Long userId) {
        UserRateLimit userLimit = getUserRateLimit(userId);
        cleanupOldRequests(userLimit);
        
        int limit = properties.botBehavior().rateLimitPerMinute();
        return userLimit.getRequestCount().get() < limit;
    }
    
    @Override
    public void recordRequest(Long userId) throws RateLimitExceededException {
        if (!canMakeRequest(userId)) {
            int limit = properties.botBehavior().rateLimitPerMinute();
            long timeUntilReset = getTimeUntilReset(userId);
            throw new RateLimitExceededException(
                String.format("Rate limit exceeded. Limit: %d requests per minute. Try again in %d seconds.", 
                    limit, timeUntilReset), 
                limit, 1
            );
        }
        
        UserRateLimit userLimit = getUserRateLimit(userId);
        userLimit.getRequestCount().incrementAndGet();
        log.debug("Recorded request for user {}, count: {}", userId, userLimit.getRequestCount().get());
    }
    
    @Override
    public int getRemainingRequests(Long userId) {
        UserRateLimit userLimit = getUserRateLimit(userId);
        cleanupOldRequests(userLimit);
        
        int limit = properties.botBehavior().rateLimitPerMinute();
        int used = userLimit.getRequestCount().get();
        return Math.max(0, limit - used);
    }
    
    @Override
    public long getTimeUntilReset(Long userId) {
        UserRateLimit userLimit = getUserRateLimit(userId);
        LocalDateTime resetTime = userLimit.getWindowStart().plusMinutes(1);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(resetTime)) {
            return 0;
        }
        
        return ChronoUnit.SECONDS.between(now, resetTime);
    }
    
    @Override
    public void resetRateLimit(Long userId) {
        rateLimits.remove(userId);
        log.info("Reset rate limit for user {}", userId);
    }
    
    private UserRateLimit getUserRateLimit(Long userId) {
        return rateLimits.computeIfAbsent(userId, id -> new UserRateLimit());
    }
    
    private void cleanupOldRequests(UserRateLimit userLimit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = userLimit.getWindowStart();
        
        // If more than a minute has passed, reset the window
        if (ChronoUnit.MINUTES.between(windowStart, now) >= 1) {
            userLimit.setWindowStart(now);
            userLimit.getRequestCount().set(0);
        }
    }
    
    /**
     * Internal class to track rate limit for a user
     */
    private static class UserRateLimit {
        private volatile LocalDateTime windowStart = LocalDateTime.now();
        private final AtomicInteger requestCount = new AtomicInteger(0);
        
        public LocalDateTime getWindowStart() {
            return windowStart;
        }
        
        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }
        
        public AtomicInteger getRequestCount() {
            return requestCount;
        }
    }
}
