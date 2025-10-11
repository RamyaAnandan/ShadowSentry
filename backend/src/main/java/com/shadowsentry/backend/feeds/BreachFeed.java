package com.shadowsentry.backend.feeds;

import java.util.List;

import com.shadowsentry.backend.incident.BreachIncident;

public interface BreachFeed {
    /**
     * Fetch breach incidents for the given email (or return empty list).
     * Implementations must not throw for "not found" — return empty list.
     */
    List<BreachIncident> fetchByEmail(String email) throws Exception;

    /**
     * Short name for the feed (e.g. "HIBP")
     */
    String name();
}
