package com.shadowsentry.backend.feeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shadowsentry.backend.incident.BreachIncident;

/**
 * ✅ HIBPFeedController — handles direct requests to the HaveIBeenPwned feed
 * Used for manual testing or feed-level lookups.
 */
@RestController
@RequestMapping("/api/feeds/hibp")
@CrossOrigin(origins = "http://localhost:5173") // Frontend allowed
public class HIBPController {

    private final HIBPFeedService hibpFeedService;

    public HIBPController(HIBPFeedService hibpFeedService) {
        this.hibpFeedService = hibpFeedService;
    }

    /**
     * 🔹 GET /api/feeds/hibp/ping — quick test endpoint
     */
    @GetMapping("/ping")
    public String ping() {
        return "✅ HIBPFeedController is running successfully!";
    }

    /**
     * 🔹 GET /api/feeds/hibp?email=<email>
     * Returns breach data and a computed risk score for the given email.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        List<BreachIncident> incidents = hibpFeedService.fetchByEmail(email);

        int breachCount = incidents.size();
        int riskScore = 0;
        if (!incidents.isEmpty()) {
            int totalRisk = incidents.stream().mapToInt(BreachIncident::getRiskScore).sum();
            riskScore = Math.min(100, (totalRisk / breachCount) + (breachCount * 5));
        }

        List<String> breachNames = new ArrayList<>();
        for (BreachIncident inc : incidents) {
            breachNames.add(inc.getSource());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("email", email);
        response.put("breachCount", breachCount);
        response.put("riskScore", riskScore);
        response.put("breaches", breachNames);

        return ResponseEntity.ok(response);
    }
}
