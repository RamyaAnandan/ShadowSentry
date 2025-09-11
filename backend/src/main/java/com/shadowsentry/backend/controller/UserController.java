package com.shadowsentry.backend.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    // ✅ Protected endpoint - requires a valid JWT
   @GetMapping("/me")
public Map<String, Object> me(Authentication authentication) {
    if (authentication == null) {
        return Map.of("error", "Not authenticated");
    }
    return Map.of(
        "username", authentication.getName(),
        "authorities", authentication.getAuthorities(),
        "details", authentication.getDetails(),
        "principal", authentication.getPrincipal()
    );
}

}
