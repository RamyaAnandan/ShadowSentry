package com.shadowsentry.backend.watchlist;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Watchlist items created by users to monitor identifiers.
 * type: email | domain | username | phone
 */
@Document(collection = "watchlist")
public class WatchlistItem {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String type; // email, domain, username, phone

    @Indexed
    private String value; // normalized value (lowercase for email/domain)

    private Instant createdAt;
    private Instant lastCheckedAt;
    private boolean active;

    // --- Constructors ---
    public WatchlistItem() {
    }

    public WatchlistItem(String id, String userId, String type, String value,
                         Instant createdAt, Instant lastCheckedAt, boolean active) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.createdAt = createdAt;
        this.lastCheckedAt = lastCheckedAt;
        this.active = active;
    }

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
