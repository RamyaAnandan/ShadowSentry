package com.shadowsentry.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shadowsentry.backend.model.RefreshToken;
import com.shadowsentry.backend.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Create new refresh token for a user
    public RefreshToken createRefreshToken(String userId, String ip, String userAgent) {
        String tokenValue = UUID.randomUUID().toString(); // random token
        String tokenHash = tokenValue; // 🔒 ideally, hash this before saving

        // ✅ Using the 7-arg constructor from RefreshToken.java
        RefreshToken refreshToken = new RefreshToken(
    userId,
    tokenHash,
    new Date(), // createdAt
    Date.from(Instant.now().plus(7, ChronoUnit.DAYS)), // expiresAt
    false,       // revoked
    null,        // replacedBy
    ip,          // IP
    userAgent    // User agent
);


        return refreshTokenRepository.save(refreshToken);
    }

    // Validate refresh token (exists, not expired, not revoked)
    public Optional<RefreshToken> validateRefreshToken(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash)
            .filter(token -> !token.isRevoked() &&
                             token.getExpiresAt().after(new Date()));
    }

    // Revoke all refresh tokens for a user (on logout)
    public void revokeAllTokensForUser(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
