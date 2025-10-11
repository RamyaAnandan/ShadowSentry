package com.shadowsentry.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.shadowsentry.backend.model.Leak;

/**
 * MongoDB repository for leaks.
 * Provides CRUD + finder by fingerprint.
 */
@Repository
public interface LeakRepository extends MongoRepository<Leak, String> {
    Optional<Leak> findByFingerprint(String fingerprint);
}
