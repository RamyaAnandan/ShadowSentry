package com.shadowsentry.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shadowsentry.backend.auth.dto.LeakDto;
import com.shadowsentry.backend.model.Leak;
import com.shadowsentry.backend.service.LeakIngestService;

/**
 * REST controller to ingest leaks (from datasets, crawler, or synthetic generator).
 */
@RestController
@RequestMapping("/api/leaks")
public class LeakIngestController {

    private final LeakIngestService leakIngestService;

    public LeakIngestController(LeakIngestService leakIngestService) {
        this.leakIngestService = leakIngestService;
    }

    /**
     * Ingest a single leak.
     */
    @PostMapping
    public ResponseEntity<Leak> ingestLeak(@RequestBody LeakDto dto) {
        Leak saved = leakIngestService.ingest(dto);
        return ResponseEntity.ok(saved);
    }

    /**
     * Ingest a batch of leaks (array of LeakDto).
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Leak>> ingestBatch(@RequestBody List<LeakDto> dtos) {
        List<Leak> saved = dtos.stream()
                .map(leakIngestService::ingest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(saved);
    }
}
