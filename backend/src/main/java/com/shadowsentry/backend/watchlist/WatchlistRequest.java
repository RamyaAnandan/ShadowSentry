package com.shadowsentry.backend.watchlist;

import jakarta.validation.constraints.NotBlank;

public class WatchlistRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Type is required")
    private String type; // email | domain | username | phone

    @NotBlank(message = "Value is required")
    private String value;

    // --- Getters and Setters ---
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
}
