package com.shadowsentry.backend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    @NotBlank(message = "jwt.secret must not be blank")
    private String secret;

    @Min(value = 60000, message = "jwt.accessTokenExpiryMs must be at least 60,000 ms (1 minute)")
    private long accessTokenExpiryMs;

    @Min(value = 1, message = "jwt.refreshTokenExpiryDays must be at least 1 day")
    private int refreshTokenExpiryDays;

    // --- Getters & Setters ---
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }
    public void setAccessTokenExpiryMs(long accessTokenExpiryMs) {
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    public int getRefreshTokenExpiryDays() {
        return refreshTokenExpiryDays;
    }
    public void setRefreshTokenExpiryDays(int refreshTokenExpiryDays) {
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }
}
