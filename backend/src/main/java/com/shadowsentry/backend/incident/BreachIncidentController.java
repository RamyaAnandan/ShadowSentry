package com.shadowsentry.backend.incident;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shadowsentry.backend.feeds.HIBPFeedService;

/**
 * ✅ ShadowSentry — Breach Incident Controller
 * Handles ingestion, search, and risk computation endpoints.
 */
@RestController
@RequestMapping("/api/v1/incidents")
@CrossOrigin(origins = "http://localhost:5173") // React Vite frontend default port
public class BreachIncidentController {

    private static final Logger log = LoggerFactory.getLogger(BreachIncidentController.class);

    private final BreachIncidentService service;
    private final HIBPFeedService hibpFeedService;

    public BreachIncidentController(BreachIncidentService service, HIBPFeedService hibpFeedService) {
        this.service = service;
        this.hibpFeedService = hibpFeedService;
    }

    // --------------------------------------------------------------------
    // 🔹 POST /ingest → Store a new breach incident
    // --------------------------------------------------------------------
    @PostMapping("/ingest")
    public ResponseEntity<BreachIncident> ingestIncident(@RequestBody BreachIncident incoming) {
        String rawPassword = null;

        if (incoming.getMeta() != null && incoming.getMeta().get("passwordPlain") instanceof String pw) {
            rawPassword = pw;
            incoming.getMeta().remove("passwordPlain");
        }

        BreachIncident saved = service.saveIncomingIncident(incoming, rawPassword);

        if (saved.getEvidence() != null) {
            saved.getEvidence().setPasswordHash(null);
            saved.getEvidence().setPasswordRedacted(null);
        }

        log.info("✅ Incident ingested successfully for email: {}",
                saved.getEvidence() != null ? saved.getEvidence().getEmail() : "unknown");

        return ResponseEntity.ok(saved);
    }

    // --------------------------------------------------------------------
    // 🔹 GET /search → Query incidents by filters
    // --------------------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<List<BreachIncident>> search(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "watchlistId", required = false) String watchlistId,
            @RequestParam(value = "minRisk", required = false) Integer minRisk,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size
    ) {
        List<BreachIncident> results = service.search(email, domain, watchlistId, minRisk, page, size);

        results.forEach(b -> {
            if (b.getEvidence() != null) {
                b.getEvidence().setPasswordHash(null);
                b.getEvidence().setPasswordRedacted(null);
            }
        });

        log.info("🔍 Search executed with filters - email: {}, domain: {}, minRisk: {}", email, domain, minRisk);
        return ResponseEntity.ok(results);
    }

    // --------------------------------------------------------------------
    // 🔹 GET / → List incidents by email
    // --------------------------------------------------------------------
    @GetMapping("/risk")
public ResponseEntity<Map<String, Object>> getRiskForEmail(@RequestParam("email") String email) {
    Map<String, Object> response = new HashMap<>();

    try {
        log.info("🚀 /api/v1/incidents/risk CALLED for {}", email);

        // ✅ Call HIBP service
        List<BreachIncident> incidents = hibpFeedService.fetchByEmail(email);
        if (incidents == null) incidents = new ArrayList<>();

        log.info("📡 HIBPFeedService returned {} incidents for {}", incidents.size(), email);
        if (incidents.isEmpty()) {
             log.warn("⚠ No data returned — check HIBP API key or network connectivity.");
}

        // ✅ Normalize data for frontend
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (BreachIncident b : incidents) {
            Map<String, Object> map = new HashMap<>();

            // Source or domain
            map.put("source", b.getSource() != null ? b.getSource() : "Unknown Source");

            // ✅ Description fallback logic — since getSummary() or getDescription() don’t exist
            String desc = "No description available";
            if (b.getMeta() != null && b.getMeta().containsKey("desc")) {
                desc = String.valueOf(b.getMeta().get("desc"));
            } else if (b.getMeta() != null && b.getMeta().containsKey("info")) {
                desc = String.valueOf(b.getMeta().get("info"));
            }
            map.put("description", desc);

            // Date (safe)
            map.put("date", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "Unknown date");

            // Risk
            map.put("riskScore", b.getRiskScore());

            // Email (from Evidence if available)
            String mail = "unknown";
            if (b.getEvidence() != null && b.getEvidence().getEmail() != null) {
                mail = b.getEvidence().getEmail();
            }
            map.put("email", mail);

            normalized.add(map);
        }

        // ✅ Compute overall risk score
       // Balanced final risk calculation (blend severity + count)
int riskScore = 0;
if (incidents != null && !incidents.isEmpty()) {
    int count = incidents.size();

    // 1) average per-breach severity (guard nulls)
    int totalPerBreach = 0;
    for (BreachIncident b : incidents) {
        totalPerBreach += Math.max(0, b.getRiskScore());
    }
    double avgSeverity = (double) totalPerBreach / count;

    // 2) count factor — modest weight for number of breaches
    // Tune multiplier if you want more/less weight for count
    double countFactor = Math.min(100.0, count * 10.0);

    // 3) blend severity and count
    double severityWeight = 0.75; // 75% severity, 25% count by default
    double countWeight = 1.0 - severityWeight;
    double blended = (severityWeight * avgSeverity) + (countWeight * countFactor);

    // clamp & round
    riskScore = (int) Math.round(Math.max(0.0, Math.min(100.0, blended)));
} else {
    riskScore = 0;
}

// Message mapping (you can tune thresholds if desired)
String message;
if (incidents == null || incidents.isEmpty()) {
    message = "✅ No breaches found — your account appears safe.";
} else if (riskScore >= 80) {
    message = "🚨 High risk — immediate action required — rotate passwords and enable 2FA.";
} else if (riskScore >= 60) {
    message = "⚠ Moderate risk — some breach data found. Consider changing passwords.";
} else if (riskScore >= 30) {
    message = "ℹ Low-to-moderate exposure — monitor and update older passwords.";
} else {
    message = "ℹ Low risk — minimal exposure detected.";
}

        // ✅ Final response for frontend
        response.put("email", email);
        response.put("incidentCount", incidents.size());
        response.put("totalBreaches", incidents.size());
        response.put("riskScore", riskScore);
        response.put("message", message);
        response.put("incidents", normalized);

        log.info("🧠 Risk computed for {} — score: {}, incidents: {}", email, riskScore, incidents.size());
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        log.error("❌ Error fetching risk data for {}: {}", email, e.getMessage(), e);
        response.put("error", "Failed to fetch risk data for " + email);
        response.put("details", e.getMessage());
        response.put("incidents", new ArrayList<>());
        response.put("riskScore", 0);
        return ResponseEntity.internalServerError().body(response);
    }
}


    // --------------------------------------------------------------------
    // 🔹 GET /ping → Health check
    // --------------------------------------------------------------------
    @GetMapping("/ping")
    public String ping() {
        log.info("✅ Health check ping successful");
        return "✅ ShadowSentry BreachIncidentController is running fine!";
    }
}
