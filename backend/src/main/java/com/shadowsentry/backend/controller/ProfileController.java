package com.shadowsentry.backend.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null) return Map.of("authenticated", false);
        return Map.of(
                "authenticated", true,
                "username", auth.getName()
        );
    }
}
