package com.shadowsentry.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shadowsentry.backend.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Convenience: find by username OR email
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Existence checks used during registration
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
