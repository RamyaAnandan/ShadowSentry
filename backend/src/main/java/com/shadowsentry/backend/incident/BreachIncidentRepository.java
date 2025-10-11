package com.shadowsentry.backend.incident;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BreachIncidentRepository extends MongoRepository<BreachIncident, String> {
    List<BreachIncident> findByEvidenceEmail(String email);
    List<BreachIncident> findByRiskScoreGreaterThanEqual(int minRisk);
    Optional<BreachIncident> findByFingerprint(String fingerprint);
}
