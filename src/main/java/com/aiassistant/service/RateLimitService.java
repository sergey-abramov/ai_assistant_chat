package com.aiassistant.service;

import com.aiassistant.exception.RateLimitExceededException;

/**
 * Interface for rate limiting operations.
 * Follows Interface Segregation Principle - contains only rate limiting methods.
 */
public interface RateLimitService {
    
    /**
     * Check if user can make a request (within rate limit)
     * 
     * @param userId user ID
     * @return true if user can make request
     */
    boolean canMakeRequest(Long userId);
    
    /**
     * Record a request for the user
     * 
     * @param userId user ID
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    void recordRequest(Long userId) throws RateLimitExceededException;
    
    /**
     * Get remaining requests for user in current time window
     * 
     * @param userId user ID
     * @return number of remaining requests
     */
    int getRemainingRequests(Long userId);
    
    /**
     * Get time until rate limit reset (in seconds)
     * 
     * @param userId user ID
     * @return seconds until reset
     */
    long getTimeUntilReset(Long userId);
    
    /**
     * Reset rate limit for user (admin function)
     * 
     * @param userId user ID
     */
    void resetRateLimit(Long userId);
}
