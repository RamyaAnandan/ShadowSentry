package com.shadowsentry.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.shadowsentry.backend.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    // Find a refresh token by its hash (used for validation)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Get all refresh tokens belonging to a user (for /api/sessions)
    List<RefreshToken> findByUserId(String userId);

    // Delete all refresh tokens for a user (used in logout)
    void deleteByUserId(String userId);
}
