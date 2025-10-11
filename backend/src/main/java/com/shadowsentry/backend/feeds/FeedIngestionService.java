package com.shadowsentry.backend.feeds;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.shadowsentry.backend.incident.BreachIncident;
import com.shadowsentry.backend.incident.BreachIncidentService;
import com.shadowsentry.backend.incident.Evidence;

/**
 * ✅ FeedIngestionService — Central orchestrator for all external data feeds (e.g., HIBP).
 * Handles:
 *   - Unified ingestion from multiple connectors
 *   - Deduplication and fingerprinting
 *   - Secure persistence via BreachIncidentService
 *   - Graceful fallback in case of connector failure
 */
@Service
public class FeedIngestionService {

    private static final Logger log = LoggerFactory.getLogger(FeedIngestionService.class);

    private final List<BreachFeed> connectors;
    private final BreachIncidentService incidentService;

    public FeedIngestionService(List<BreachFeed> connectors, BreachIncidentService incidentService) {
        this.connectors = connectors;
        this.incidentService = incidentService;
    }

    /**
     * 🔹 Ingest a single incident from any connector.
     * Ensures deduplication and updates occurrence count on duplicates.
     */
    public BreachIncident ingestIncident(BreachIncident inc) {
        if (inc == null) {
            log.warn("⚠️ Attempted to ingest a null incident — skipping.");
            return null;
        }

        if (inc.getEvidence() == null) {
            inc.setEvidence(new Evidence());
        }

        if (inc.getEvidence().getEmail() != null) {
            inc.getEvidence().setEmail(inc.getEvidence().getEmail().toLowerCase().trim());
        }

        // ✅ Compute fingerprint
        String fp = computeFingerprint(inc);
        inc.setFingerprint(fp);

        // Ensure timestamps
        if (inc.getCreatedAt() == null) inc.setCreatedAt(Instant.now());
        if (inc.getLastSeen() == null) inc.setLastSeen(Instant.now());
        if (inc.getOccurrenceCount() == 0) inc.setOccurrenceCount(1);

        try {
            BreachIncident saved = incidentService.saveIncomingIncident(inc, null);
            log.debug("💾 Saved incident [{}] from source [{}]", fp, inc.getSource());
            return saved;
        } catch (DuplicateKeyException dk) {
            log.info("🔁 Duplicate fingerprint [{}] — incrementing occurrence count", fp);
            incidentService.incrementOccurrence(fp);
            return inc;
        } catch (RuntimeException ex) {
            log.warn("❌ Failed to save incident [{}]: {}", fp, ex.getMessage());
            return inc; // return even on failure to prevent halting feed
        }
    }

    /**
     * 🔹 Ingest from all registered connectors for a given email.
     * Returns the number of new incidents successfully saved.
     */
    public int ingestForEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("⚠️ ingestForEmail called with blank email");
            return 0;
        }

        int totalSaved = 0;

        for (BreachFeed connector : connectors) {
            try {
                log.info("🌐 Fetching incidents for [{}] from connector [{}]", email, connector.name());

                List<BreachIncident> incidents = connector.fetchByEmail(email);
                if (incidents == null || incidents.isEmpty()) {
                    log.info("ℹ️ No incidents found from connector [{}] for {}", connector.name(), email);
                    continue;
                }

                for (BreachIncident inc : incidents) {
                    ingestIncident(inc);
                    totalSaved++;
                }

                log.info("✅ Connector [{}] processed {} incidents for {}", connector.name(), incidents.size(), email);

            } catch (Exception ex) {
                log.error("💥 Connector [{}] failed for [{}]: {}", connector.name(), email, ex.getMessage(), ex);
            }
        }

        log.info("📊 Feed ingestion completed for [{}] — total saved: {}", email, totalSaved);
        return totalSaved;
    }

    /**
     * 🔹 Compute a unique fingerprint (hash) for deduplication.
     * Combines source + sourceId + email to ensure uniqueness across connectors.
     */
    private String computeFingerprint(BreachIncident inc) {
        try {
            String seed = String.join(":",
                    safe(inc.getSource()),           // replaced getTitle() with getSource()
                    safe(inc.getSourceId()),
                    safe(inc.getEvidence() != null ? inc.getEvidence().getEmail() : "")
            );

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(seed.getBytes(StandardCharsets.UTF_8));

            try (Formatter f = new Formatter()) {
                for (byte b : digest) f.format("%02x", b);
                return f.toString();
            }
        } catch (Exception e) {
            log.error("⚠️ Failed to compute fingerprint, fallback UUID: {}", e.getMessage());
            return UUID.randomUUID().toString();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
