package com.shadowsentry.backend.incident;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ✅ Evidence = sanitized proof of a breach record.
 * 
 * Rules:
 *  - Never store raw plaintext credentials.
 *  - passwordHash is internal only (not serialized in API responses).
 *  - passwordRedacted is a safe placeholder (e.g., "p****d").
 *  - details provides optional contextual information (e.g., data classes or breach summary).
 */
public class Evidence {

    private String email;

    @JsonIgnore
    private String passwordHash;

    private String passwordRedacted;

    private String phone;

    private String username;

    private String details; // ✅ NEW — breach summary or extra context

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordRedacted() {
        return passwordRedacted;
    }

    public void setPasswordRedacted(String passwordRedacted) {
        this.passwordRedacted = passwordRedacted;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
