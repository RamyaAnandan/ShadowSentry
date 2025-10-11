package com.shadowsentry.backend.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Canonical Leak document stored in MongoDB.
 * Normalized representation used by ingestion, matching and scoring.
 */
@Document(collection = "leaks")
public class Leak {

    @Id
    private String id;

    /**
     * Where this leak came from: dataset, crawler, HIBP, Dehashed, etc.
     */
    private String source;

    /**
     * Optional source-specific id (e.g. post id, file name, external id).
     */
    private String sourceId;

    /**
     * Type: credentials, email, phone, paste, db_dump, etc.
     */
    private String type;

    /**
     * Structured evidence (email/password/hash/text etc). Use Map for flexibility.
     */
    private Map<String, Object> evidence;

    /**
     * When this leak was discovered at the source (if available).
     */
    private Date discoveredAt;

    /**
     * First time we saw it in our system (ingest time).
     */
    private Date firstSeen;

    /**
     * Last time we saw it (for updates / occurrence counting).
     */
    private Date lastSeen;

    /**
     * IDs of watchlist items that matched this leak (user-specific linkage).
     */
    private List<String> matchedWatchlistIds = new ArrayList<>();

    /**
     * 0-100 risk score (computed by the scoring engine).
     */
    private Integer riskScore = 0;

    /**
     * Free-form metadata: crawler batch id, sourceUrl, country, etc.
     */
    private Map<String, Object> meta;

    /**
     * Canonical fingerprint to deduplicate identical leaks across sources.
     * Marked unique to avoid duplicates.
     */
    @Indexed(unique = true, name = "fingerprint_idx")
    private String fingerprint;

    /**
     * How many times we've seen this fingerprint (occurrence count).
     */
    private Integer occurrenceCount = 1;

    public Leak() {}

    // Convenience constructor for new leak creation
    public Leak(String source,
                String sourceId,
                String type,
                Map<String, Object> evidence,
                Date discoveredAt,
                Map<String, Object> meta,
                String fingerprint) {
        this.source = source;
        this.sourceId = sourceId;
        this.type = type;
        this.evidence = evidence;
        this.discoveredAt = discoveredAt;
        Date now = new Date();
        this.firstSeen = now;
        this.lastSeen = now;
        this.meta = meta;
        this.fingerprint = fingerprint;
        this.occurrenceCount = 1;
        this.riskScore = 0;
    }

    // ---------------- Getters and Setters ----------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getEvidence() { return evidence; }
    public void setEvidence(Map<String, Object> evidence) { this.evidence = evidence; }

    public Date getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Date discoveredAt) { this.discoveredAt = discoveredAt; }

    public Date getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Date firstSeen) { this.firstSeen = firstSeen; }

    public Date getLastSeen() { return lastSeen; }
    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }

    public List<String> getMatchedWatchlistIds() { return matchedWatchlistIds; }
    public void setMatchedWatchlistIds(List<String> matchedWatchlistIds) { this.matchedWatchlistIds = matchedWatchlistIds; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public Integer getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(Integer occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    // helper to increment occurrence + update lastSeen
    public void incrementOccurrence() {
        this.occurrenceCount = (this.occurrenceCount == null) ? 1 : this.occurrenceCount + 1;
        this.lastSeen = new Date();
    }

    // helper to add a matched watchlist id if not already present
    public void addMatchedWatchlistId(String watchlistId) {
        if (watchlistId == null) return;
        if (this.matchedWatchlistIds == null) this.matchedWatchlistIds = new ArrayList<>();
        if (!this.matchedWatchlistIds.contains(watchlistId)) this.matchedWatchlistIds.add(watchlistId);
    }

    @Override
    public String toString() {
        return "Leak{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", type='" + type + '\'' +
                ", fingerprint='" + fingerprint + '\'' +
                ", occurrenceCount=" + occurrenceCount +
                '}';
    }
}
