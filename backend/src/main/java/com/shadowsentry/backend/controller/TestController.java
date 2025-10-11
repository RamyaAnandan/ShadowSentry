// package com.shadowsentry.backend.controller;

// import java.util.Map;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class TestController {

//     @GetMapping("/api/protected")
//     public ResponseEntity<Map<String, String>> protectedEndpoint() {
//         return ResponseEntity.ok(Map.of(
//                 "status", "success",
//                 "message", "You have accessed a protected resource 🚀"
//         ));
//     }
// }


package com.shadowsentry.backend.controller;  // 👈 adjust if you use another package

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/protected")
    public ResponseEntity<Map<String, String>> protectedEndpoint() {
        System.out.println("✅ Protected endpoint hit!");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "You have accessed a protected resource 🚀"
        ));
    }
}

