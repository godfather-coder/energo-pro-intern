package com.example.mssqll.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentMap<String, Date> blacklist = new ConcurrentHashMap<>();

    /**
     * Adds a token to the blacklist with its expiration date.
     *
     * @param token        The JWT token to blacklist.
     * @param expiryDate   The expiration date of the token.
     */
    public void blacklistToken(String token, Date expiryDate) {
        blacklist.put(token, expiryDate);
    }

    /**
     * Checks if a token is blacklisted.
     *
     * @param token The JWT token to check.
     * @return True if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Scheduled task to remove expired tokens from the blacklist every hour.
     * This prevents the blacklist from growing indefinitely.
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
    public void removeExpiredTokens() {
        Date now = new Date();
        blacklist.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}