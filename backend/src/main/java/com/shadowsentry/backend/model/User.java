package com.shadowsentry.backend.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String passwordHash;

    private Set<String> roles = new HashSet<>();

    private Date createdAt;
    private Date lastLogin;

    public User() {
        // no-args constructor
    }

    public User(String id, String username, String email, String passwordHash,
                Set<String> roles, Date createdAt, Date lastLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = (roles != null) ? roles : new HashSet<>();
 // ✅ use setter to normalize ROLE_ prefix
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // --- getters & setters ---

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

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

    public Set<String> getRoles() {
        return roles;
    }

    // ✅ normalize: always ensure ROLE_ prefix
    public void setRoles(Set<String> roles) {
        this.roles = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r != null && !r.isBlank()) {
                    this.roles.add(r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase());
                }
            }
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
