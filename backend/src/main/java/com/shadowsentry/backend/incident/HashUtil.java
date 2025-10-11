package com.shadowsentry.backend.incident;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    /**
     * Hash string using SHA-256 and return hex string.
     * 
     * @param input plain string
     * @return hex representation of SHA-256 digest
     */
    public static String sha256Hex(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Redact password for safe display:
     * - Show first 2 characters
     * - Mask the middle with '*'
     * - Show last 2 characters
     * Example: "Secret123!" → "Se******3!"
     * 
     * @param s plain password
     * @return redacted string
     */
    public static String redact(String s) {
        if (s == null || s.isEmpty()) return null;
        int len = s.length();
        if (len <= 4) {
            return "*".repeat(len); // e.g., "abc" → "***"
        }
        return s.substring(0, 2) + "*".repeat(len - 4) + s.substring(len - 2);
    }

    /**
     * Generate a deterministic fingerprint for deduplication.
     * Combines multiple fields into one hash seed.
     *
     * @param values values to concatenate
     * @return SHA-256 fingerprint
     */
    public static String fingerprint(String... values) {
        if (values == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            sb.append(v == null ? "" : v).append("|");
        }
        return sha256Hex(sb.toString());
    }
}
