package com.shadowsentry.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentry.backend.auth.dto.LeakDto;
import com.shadowsentry.backend.model.Leak;
import com.shadowsentry.backend.repository.LeakRepository;
import com.shadowsentry.backend.watchlist.WatchlistItem;
import com.shadowsentry.backend.watchlist.WatchlistService;

/**
 * Service to ingest leaks from datasets, crawlers, or APIs.
 * Handles fingerprinting, deduplication, and watchlist matching.
 */
@Service
public class LeakIngestService {

    private final LeakRepository leakRepository;
    private final WatchlistService watchlistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeakIngestService(LeakRepository leakRepository,
                             WatchlistService watchlistService) {
        this.leakRepository = leakRepository;
        this.watchlistService = watchlistService;
    }

    /**
     * Ingest a new leak (or update existing if fingerprint already present).
     * Also checks for matches against user watchlists.
     */
    public Leak ingest(LeakDto dto) {
        String fingerprint = generateFingerprint(dto);

        Leak leak = leakRepository.findByFingerprint(fingerprint)
                .map(existing -> {
                    existing.incrementOccurrence();
                    existing.setLastSeen(new Date());
                    return existing;
                })
                .orElseGet(() -> new Leak(
                        dto.getSource(),
                        dto.getSourceId(),
                        dto.getType(),
                        dto.getEvidence(),
                        dto.getDiscoveredAt() != null ? dto.getDiscoveredAt() : new Date(),
                        dto.getMeta(),
                        fingerprint
                ));

        // 🔍 Check watchlist matches (currently only email evidence)
        matchLeakAgainstWatchlist(leak);

        return leakRepository.save(leak);
    }

    /**
     * Match leak evidence against watchlist items.
     */
    private void matchLeakAgainstWatchlist(Leak leak) {
        if (leak.getEvidence() == null) return;

        Object emailObj = leak.getEvidence().get("email");
        if (emailObj != null) {
            String email = emailObj.toString();
            List<WatchlistItem> matches = watchlistService.findMatches(email);
            for (WatchlistItem item : matches) {
                leak.addMatchedWatchlistId(item.getId());
            }
        }

        // ✅ Extend later for phone, username, domain, etc.
    }

    /**
     * Generate a canonical fingerprint (hash) for deduplication.
     */
    private String generateFingerprint(LeakDto dto) {
        try {
            String raw = dto.getSource() + "|" +
                         dto.getType() + "|" +
                         objectMapper.writeValueAsString(dto.getEvidence());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating fingerprint (JSON)", e);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
