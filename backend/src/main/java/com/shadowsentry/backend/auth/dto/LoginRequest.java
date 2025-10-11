package com.shadowsentry.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest DTO — used for /api/v1/auth/login
 * Accepts either a username or an email along with a password.
 */
public class LoginRequest {

    /**
     * Either username or email is accepted here (frontend sends usernameOrEmail).
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    /**
     * Password used for authentication.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Optional explicit email field (used if frontend separates fields).
     */
    private String email;

    // --- Getters and Setters ---

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
