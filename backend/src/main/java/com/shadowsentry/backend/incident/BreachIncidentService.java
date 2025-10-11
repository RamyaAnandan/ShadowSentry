package com.shadowsentry.backend.incident;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.shadowsentry.backend.feeds.HIBPFeedService;

/**
 * ✅ BreachIncidentService — Unified logic for incidents, risk scoring, and HIBP integration.
 * - First checks DB cache
 * - Falls back to HIBP API via HIBPFeedService
 * - Saves new incidents
 * - Computes consistent riskScore used by Dashboard frontend
 */
@Service
public class BreachIncidentService {

    private static final Logger log = LoggerFactory.getLogger(BreachIncidentService.class);

    private final BreachIncidentRepository repository;
    private final HIBPFeedService hibpFeedService;

    public BreachIncidentService(BreachIncidentRepository repository,
                                 HIBPFeedService hibpFeedService) {
        this.repository = repository;
        this.hibpFeedService = hibpFeedService;
    }

    // --------------------------------------------------------------------
    // 🔹 Save incoming incident (used by /ingest endpoint)
    // --------------------------------------------------------------------
    public BreachIncident saveIncomingIncident(BreachIncident incoming, String rawPassword) {
        if (incoming.getFingerprint() == null || incoming.getFingerprint().isBlank()) {
            incoming.setFingerprint(UUID.randomUUID().toString());
        }
        if (incoming.getCreatedAt() == null) incoming.setCreatedAt(Instant.now());
        if (incoming.getRiskScore() == 0) incoming.setRiskScore(50);
        if (incoming.getOccurrenceCount() == 0) incoming.setOccurrenceCount(1);

        if (rawPassword != null && incoming.getEvidence() != null) {
            incoming.getEvidence().setPasswordHash("HASHED_" + rawPassword.hashCode());
            incoming.getEvidence().setPasswordRedacted(maskPassword(rawPassword));
        }

        try {
            return repository.save(incoming);
        } catch (DuplicateKeyException dk) {
            log.warn("⚠️ Duplicate incident fingerprint detected: {}", incoming.getFingerprint());
            throw dk;
        }
    }

    private String maskPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 2) return "****";
        return rawPassword.charAt(0) + "****" + rawPassword.charAt(rawPassword.length() - 1);
    }

    // --------------------------------------------------------------------
    // 🔹 Find all breaches by email
    // --------------------------------------------------------------------
    public List<BreachIncident> findByEmail(String email) {
        return repository.findByEvidenceEmail(email);
    }

    // --------------------------------------------------------------------
    // 🔹 Increment occurrence count for repeated fingerprints
    // --------------------------------------------------------------------
    public void incrementOccurrence(String fingerprint) {
        repository.findByFingerprint(fingerprint).ifPresentOrElse(b -> {
            b.setOccurrenceCount(b.getOccurrenceCount() + 1);
            b.setLastSeen(Instant.now());
            repository.save(b);
        }, () -> log.error("❌ Fingerprint not found: {}", fingerprint));
    }

    // --------------------------------------------------------------------
    // 🔹 Fetch incidents (check DB first, fallback to live HIBP)
    // --------------------------------------------------------------------
    public List<BreachIncident> fetchFromDbOrHibp(String email) {
        try {
            // Step 1️⃣: Try cached incidents from DB
            List<BreachIncident> fromDb = repository.findByEvidenceEmail(email);
            if (fromDb != null && !fromDb.isEmpty()) {
                log.info("✅ Found {} cached incidents for {}", fromDb.size(), email);
                return fromDb;
            }

            // Step 2️⃣: Fetch fresh data from HIBPFeedService
            log.info("🌐 Fetching fresh incidents from HIBP for {}", email);
            List<BreachIncident> fromHibp = hibpFeedService.fetchByEmail(email);

            if (fromHibp == null || fromHibp.isEmpty()) {
                log.info("ℹ️ No breaches found for {} via HIBPFeedService", email);
                return Collections.emptyList();
            }

            // Step 3️⃣: Save results into MongoDB for caching
            List<BreachIncident> savedList = new ArrayList<>();
            for (BreachIncident inc : fromHibp) {
                try {
                    if (inc.getFingerprint() == null)
                        inc.setFingerprint(UUID.randomUUID().toString());
                    if (inc.getCreatedAt() == null)
                        inc.setCreatedAt(Instant.now());
                    if (inc.getLastSeen() == null)
                        inc.setLastSeen(Instant.now());
                    if (inc.getEvidence() != null && inc.getEvidence().getEmail() == null)
                        inc.getEvidence().setEmail(email);

                    BreachIncident saved = repository.save(inc);
                    savedList.add(saved);
                } catch (Exception e) {
                    log.error("⚠️ Failed to save incident for {}: {}", email, e.getMessage());
                }
            }

            log.info("✅ Saved {} new incidents from HIBP for {}", savedList.size(), email);
            return savedList;

        } catch (Exception e) {
            log.error("💥 Error fetching incidents for {}: {}", email, e.getMessage());
            return Collections.emptyList();
        }
    }

    // --------------------------------------------------------------------
    // 🔹 Compute unified risk score (used by /risk endpoint)
    // --------------------------------------------------------------------
    public int computeRiskScoreFromIncidents(List<BreachIncident> incidents) {
        if (incidents == null || incidents.isEmpty()) return 0;

        int total = incidents.stream().mapToInt(BreachIncident::getRiskScore).sum();
        int avg = total / incidents.size();
        int bonus = Math.min(30, incidents.size() * 5);
        return Math.min(100, (avg / 2) + bonus + 40);
    }

    // --------------------------------------------------------------------
    // 🔹 Search incidents (for /search endpoint)
    // --------------------------------------------------------------------
    public List<BreachIncident> search(
            String email,
            String domain,
            String watchlistId,
            Integer minRisk,
            int page,
            int size
    ) {
        if (email != null && !email.isBlank()) {
            return repository.findByEvidenceEmail(email);
        }
        if (minRisk != null) {
            return repository.findByRiskScoreGreaterThanEqual(minRisk);
        }
        return repository.findAll();
    }
}
