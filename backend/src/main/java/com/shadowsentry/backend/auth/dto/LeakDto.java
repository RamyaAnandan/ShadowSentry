package com.shadowsentry.backend.auth.dto;
//package com.shadowsentry.backend.dto;

import java.util.Date;
import java.util.Map;

/**
 * DTO for ingesting a leak from JSON (synthetic, crawler, API).
 * This keeps the API layer decoupled from the DB entity (Leak).
 */
public class LeakDto {

    private String source;
    private String sourceId;
    private String type;
    private Map<String, Object> evidence;
    private Date discoveredAt;
    private Map<String, Object> meta;

    public LeakDto() {}

    public LeakDto(String source,
                   String sourceId,
                   String type,
                   Map<String, Object> evidence,
                   Date discoveredAt,
                   Map<String, Object> meta) {
        this.source = source;
        this.sourceId = sourceId;
        this.type = type;
        this.evidence = evidence;
        this.discoveredAt = discoveredAt;
        this.meta = meta;
    }

    // ---------------- Getters and Setters ----------------
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

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
