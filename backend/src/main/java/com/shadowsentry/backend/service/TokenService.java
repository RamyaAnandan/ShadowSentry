package com.shadowsentry.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shadowsentry.backend.model.RefreshToken;
import com.shadowsentry.backend.model.User;
import com.shadowsentry.backend.repository.RefreshTokenRepository;
import com.shadowsentry.backend.repository.UserRepository;
import com.shadowsentry.backend.security.JwtService;

/**
 * ✅ TokenService
 * Handles JWT and refresh token creation, validation, rotation, and cleanup.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final long refreshTokenExpiryMs;

    public TokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${jwt.refreshTokenExpiryDays}") long refreshTokenExpiryDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenExpiryMs = refreshTokenExpiryDays * 24L * 60L * 60L * 1000L;
    }

    // ---------------------------------------------------------------------
    // 🔹 Generate both Access + Refresh token pair (for login API)
    // ---------------------------------------------------------------------
    public TokenPair generateTokenPair(User user, String ip, String userAgent) {
        try {
            // 1️⃣ Delete old active tokens for this user (avoid duplicates)
            List<RefreshToken> oldTokens = refreshTokenRepository.findByUserId(user.getId());
            for (RefreshToken t : oldTokens) {
                if (!t.isRevoked()) {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                }
            }

            // 2️⃣ Generate new tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRoles());
            String refreshTokenPlain = generateRefreshTokenPlain();

            // 3️⃣ Persist hashed refresh token
            saveRefreshToken(user.getId(), refreshTokenPlain, ip, userAgent);

            log.info("🎟️ Generated new token pair for user={} (id={})", user.getUsername(), user.getId());
            return new TokenPair(accessToken, refreshTokenPlain, user);
        } catch (Exception e) {
            log.error("💥 Failed to generate token pair for {}: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate tokens", e);
        }
    }

    // ---------------------------------------------------------------------
    // 🔹 Access Token utilities
    // ---------------------------------------------------------------------
    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRoles());
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    public String extractUserId(String token) {
        return jwtService.extractUserId(token);
    }

    public List<String> extractRoles(String token) {
        return jwtService.extractRoles(token);
    }

    public boolean validateToken(String token, String expectedUserId) {
        if (token == null || expectedUserId == null) return false;
        try {
            if (!jwtService.isTokenValid(token)) return false;
            String tokenUserId = jwtService.extractUserId(token);
            boolean ok = expectedUserId.equals(tokenUserId);
            if (!ok)
                log.warn("❌ Token userId mismatch. expected={}, tokenUserId={}", expectedUserId, tokenUserId);
            return ok;
        } catch (Exception e) {
            log.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // 🔹 Refresh Token primitives
    // ---------------------------------------------------------------------
    public String generateRefreshTokenPlain() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | IllegalArgumentException ex) {
            throw new RuntimeException("Failed to hash token", ex);
        }
    }

    public RefreshToken saveRefreshToken(String userId, String tokenPlain, String ip, String userAgent) {
        String tokenHash = hashToken(tokenPlain);
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + refreshTokenExpiryMs);

        RefreshToken doc = new RefreshToken(userId, tokenHash, now, expiresAt, false, null, ip, userAgent);
        RefreshToken saved = refreshTokenRepository.save(doc);
        log.info("💾 Saved refresh token for userId={} tokenId={}", userId, saved.getId());
        return saved;
    }

    // ---------------------------------------------------------------------
    // 🔹 Refresh Token Rotation (used for /api/v1/auth/refresh)
    // ---------------------------------------------------------------------
    public RotateResult rotateRefreshToken(String oldTokenPlain) throws TokenException {
        String oldHash = hashToken(oldTokenPlain);
        Optional<RefreshToken> optOld = refreshTokenRepository.findByTokenHash(oldHash);
        if (optOld.isEmpty()) {
            throw new TokenException("Refresh token not found");
        }

        RefreshToken old = optOld.get();
        if (old.isRevoked()) {
            revokeAllForUser(old.getUserId());
            throw new TokenException("Token replay detected — all sessions revoked");
        }
        if (old.getExpiresAt() != null && old.getExpiresAt().before(new Date())) {
            throw new TokenException("Refresh token expired");
        }

        User user = userRepository.findById(old.getUserId())
                .orElseThrow(() -> new TokenException("User not found"));

        String newPlain = generateRefreshTokenPlain();
        String newHash = hashToken(newPlain);
        Date newExpiry = new Date(System.currentTimeMillis() + refreshTokenExpiryMs);

        RefreshToken newToken = new RefreshToken(
                user.getId(),
                newHash,
                new Date(),
                newExpiry,
                false,
                null,
                old.getIp(),
                old.getUserAgent()
        );

        RefreshToken savedNew = refreshTokenRepository.save(newToken);

        old.setRevoked(true);
        old.setReplacedBy(savedNew.getId());
        refreshTokenRepository.save(old);

        String accessToken = generateAccessToken(user);
        log.info("🔁 Rotated refresh token for {} (newTokenId={})", user.getUsername(), savedNew.getId());

        return new RotateResult(newPlain, accessToken, user);
    }

    // ---------------------------------------------------------------------
    // 🔹 Revoke helpers
    // ---------------------------------------------------------------------
    public void revokeRefreshToken(String tokenPlain) {
        String hash = hashToken(tokenPlain);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
            log.info("❌ Revoked refresh token tokenId={} userId={}", t.getId(), t.getUserId());
        });
    }

    public void revokeAllForUser(String userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        for (RefreshToken t : tokens) {
            if (!t.isRevoked()) {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            }
        }
        log.info("🧹 Revoked all refresh tokens for userId={}", userId);
    }

    // ---------------------------------------------------------------------
    // 🔹 DTOs
    // ---------------------------------------------------------------------
    public static class TokenPair {
        public final String accessToken;
        public final String refreshToken;
        public final User user;

        public TokenPair(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }

    public static class RotateResult {
        public final String newRefreshToken;
        public final String accessToken;
        public final User user;

        public RotateResult(String newRefreshToken, String accessToken, User user) {
            this.newRefreshToken = newRefreshToken;
            this.accessToken = accessToken;
            this.user = user;
        }
    }

    public static class TokenException extends Exception {
        public TokenException(String message) {
            super(message);
        }
    }
}
