package com.shadowsentry.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shadowsentry.backend.model.RefreshToken;
import com.shadowsentry.backend.repository.RefreshTokenRepository;

@RestController
@RequestMapping("/api/sessions")
public class SessionsController {

    private final RefreshTokenRepository refreshTokenRepository;

    public SessionsController(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * List all active sessions (refresh tokens) for the logged-in user
     */
    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        String userId = authentication.getName();
        List<RefreshToken> sessions = refreshTokenRepository.findByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Revoke a specific session (refresh token) by its ID
     */
    @PostMapping("/{id}/revoke")
    public ResponseEntity<?> revoke(@PathVariable("id") String sessionId, Authentication authentication) {
        String userId = authentication.getName();

        return refreshTokenRepository.findById(sessionId)
                .map(doc -> {
                    if (!doc.getUserId().equals(userId)) {
                        return ResponseEntity.status(403).body(Map.of("error", "Not authorized to revoke this session"));
                    }
                    doc.setRevoked(true);
                    refreshTokenRepository.save(doc);
                    return ResponseEntity.ok(Map.of("ok", true));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Session not found")));
    }
}
