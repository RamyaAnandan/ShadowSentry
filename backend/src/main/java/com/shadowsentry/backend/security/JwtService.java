package com.shadowsentry.backend.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.shadowsentry.backend.config.AuthProperties;
import com.shadowsentry.backend.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final JwtProperties jwtProps;
    private final AuthProperties authProps;

    public JwtService(JwtProperties jwtProps, AuthProperties authProps) {
        if (jwtProps.getSecret() == null || jwtProps.getSecret().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters for HS256");
        }
        this.jwtProps = jwtProps;
        this.authProps = authProps;

        byte[] keyBytes = Decoders.BASE64.decode(jwtProps.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Generate Access Token (JJWT 0.11.x syntax)
    public String generateAccessToken(String userId, String username, Collection<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProps.getAccessTokenExpiryMs());

        List<String> normRoles = (roles == null)
                ? List.of("ROLE_USER")
                : roles.stream().map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r).toList();

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer("ShadowSentry-AuthServer")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(Map.of(
                        "userId", userId,
                        "authorities", normRoles
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.debug("✅ Generated access token for {} expiring in {}ms", username, jwtProps.getAccessTokenExpiryMs());
        return token;
    }

    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProps.getRefreshTokenExpiryDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuer("ShadowSentry-AuthServer")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        try {
            Object val = parseClaims(token).get("userId");
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            log.debug("Failed to extract userId: {}", e.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            log.debug("Failed to extract username: {}", e.getMessage());
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Object roles = parseClaims(token).get("authorities");
            if (roles instanceof List<?> roleList)
                return roleList.stream().map(Object::toString).toList();
        } catch (Exception e) {
            log.debug("Failed to extract roles: {}", e.getMessage());
        }
        return List.of();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseClaims(token);
            String subject = claims.getSubject();
            return subject != null && subject.equals(expectedUsername)
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token for {}: {}", expectedUsername, e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessTokenExpiryMs() { return jwtProps.getAccessTokenExpiryMs(); }
    public long getRefreshTokenExpiryDays() { return jwtProps.getRefreshTokenExpiryDays(); }
    public String getRefreshCookieName() { return authProps.getRefreshCookieName(); }
}
