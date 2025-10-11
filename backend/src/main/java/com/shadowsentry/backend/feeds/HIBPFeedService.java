
package com.shadowsentry.backend.feeds;

import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentry.backend.incident.BreachIncident;
import com.shadowsentry.backend.incident.Evidence;

@Service
public class HIBPFeedService implements BreachFeed {

    private static final Logger log = LoggerFactory.getLogger(HIBPFeedService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${hibp.api.key:}")
    private String hibpApiKey;

    @Value("${hibp.baseUrl:https://haveibeenpwned.com/api/v3}")
    private String hibpBaseUrl;

    @Value("${app.user.agent:ShadowSentryApp/1.0 (contact@shadowsentry.io)}")
    private String userAgent;

    public HIBPFeedService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String name() {
        return "HIBPFeed";
    }

    @Override
    public List<BreachIncident> fetchByEmail(String email) {
        List<BreachIncident> incidents = new ArrayList<>();
        if (email == null || email.isBlank()) return incidents;

        try {
            if (hibpApiKey == null || hibpApiKey.isBlank()) {
                log.error("❌ Missing HIBP API key in application.properties");
                return incidents;
            }

            // ✅ FIX: Do NOT encode manually
            String url = hibpBaseUrl + "/breachedaccount/" + email.trim() +
                    "?truncateResponse=false&includeUnverified=true";

            HttpHeaders headers = new HttpHeaders();
            headers.set("hibp-api-key", hibpApiKey.trim());
            headers.set("User-Agent", userAgent);
            headers.set("Accept", "application/json");

            log.info("🌐 Querying HIBP endpoint: {}", url);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            int status = resp.getStatusCode().value();

            log.info("📩 Response Code: {}", status);
            log.info("📩 Raw Body (first 200 chars): {}", 
                resp.getBody() == null ? "<empty>" : resp.getBody().substring(0, Math.min(200, resp.getBody().length())));

            if (status == 404) {
                log.info("ℹ️ No breaches found for {}", email);
                return incidents;
            }
            if (status == 403) {
                log.error("🚫 Invalid HIBP API key or missing permission");
                return incidents;
            }
            if (status == 429) {
                log.warn("⚠️ Rate limited by HIBP — please wait before retrying");
                return incidents;
            }
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.error("🚫 Non-success response {} from HIBP", status);
                return incidents;
            }

            String body = resp.getBody();
            if (body == null || body.isBlank()) {
                log.warn("⚠️ Empty HIBP response body for {}", email);
                return incidents;
            }

            // ✅ Parse breaches safely
            List<Map<String, Object>> breaches = mapper.readValue(body, new TypeReference<>() {});
            log.info("✅ Parsed {} breaches for {}", breaches.size(), email);

            for (Map<String, Object> b : breaches) {
                BreachIncident inc = new BreachIncident();
                inc.setFingerprint(UUID.randomUUID().toString());

                String title = Optional.ofNullable(b.get("Title"))
                        .orElse(Optional.ofNullable(b.get("Name")).orElse("Unknown"))
                        .toString();

                inc.setSource(title);
                inc.setRiskScore(calculateRisk(b.get("PwnCount")));
                inc.setCreatedAt(Instant.now());
                inc.setLastSeen(Instant.now());
                inc.setOccurrenceCount(1);

                Evidence e = new Evidence();
                e.setEmail(email);
                e.setDetails(Optional.ofNullable(b.get("Description"))
                        .map(Object::toString)
                        .orElse(title + " breach"));
                inc.setEvidence(e);

                incidents.add(inc);
            }

        } catch (HttpClientErrorException ex) {
            log.error("⚠️ HTTP error during HIBP call: {} - {}", ex.getStatusCode(), ex.getMessage());
        } catch (Exception ex) {
            log.error("💥 Unexpected exception fetching breaches: {}", ex.getMessage(), ex);
        }

        log.info("✅ Returning {} incidents for {}", incidents.size(), email);
        return incidents;
    }

    private int calculateRisk(Object pwn) {
    if (pwn instanceof Number n) {
        int c = n.intValue();
        // smaller scale so a single large breach doesn't blow the score up too high
        // log10 scale reduces huge counts to manageable range
        double scaled = Math.log10(Math.max(1, c)) * 8.0;   // tune multiplier (8)
        // base offset to ensure a minimum score for any found breach
        int risk = (int) Math.round(scaled + 10.0);         // base 10
        // clamp to a sensible range
        return Math.max(5, Math.min(95, risk));
    }
    return 8; // default for unknown pwn count
}

}
