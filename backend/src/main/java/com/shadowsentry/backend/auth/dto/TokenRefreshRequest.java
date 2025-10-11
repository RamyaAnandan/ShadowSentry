package com.shadowsentry.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * TokenRefreshRequest — DTO for /api/v1/auth/refresh
 * Expected payload:
 * {
 *   "refreshToken": "string"
 * }
 */
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public TokenRefreshRequest() {
        // Default constructor
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
