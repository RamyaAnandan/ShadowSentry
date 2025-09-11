package com.shadowsentry.backend.auth.dto;

public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    // ✅ Factory method for cleaner usage
    public static AuthResponse bearer(String token, long expiresIn) {
        return new AuthResponse(token, expiresIn);
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
