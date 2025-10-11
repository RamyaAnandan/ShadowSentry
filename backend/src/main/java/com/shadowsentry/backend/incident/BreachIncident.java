package com.shadowsentry.backend.incident;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "breach_incidents")
public class BreachIncident {

    @Id
    private String id;

    private String source;
    private String sourceId;
    private String type;
    private Evidence evidence;
    private Instant discoveredAt;
    private Instant firstSeen;
    private Instant lastSeen;
    private List<String> matchedWatchlistIds;
    private int riskScore;
    private Map<String, Object> meta;

    @Indexed(unique = true)
    private String fingerprint;

    private int occurrenceCount;

    @CreatedDate
    private Instant createdAt;

    private List<String> linkedUserIds;

    // --- getters and setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Evidence getEvidence() { return evidence; }
    public void setEvidence(Evidence evidence) { this.evidence = evidence; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }

    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }

    public List<String> getMatchedWatchlistIds() { return matchedWatchlistIds; }
    public void setMatchedWatchlistIds(List<String> matchedWatchlistIds) { this.matchedWatchlistIds = matchedWatchlistIds; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<String> getLinkedUserIds() { return linkedUserIds; }
    public void setLinkedUserIds(List<String> linkedUserIds) { this.linkedUserIds = linkedUserIds; }
}
