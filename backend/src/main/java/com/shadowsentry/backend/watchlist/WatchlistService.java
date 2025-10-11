package com.shadowsentry.backend.watchlist;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

    private final WatchlistRepository repository;

    public WatchlistService(WatchlistRepository repository) {
        this.repository = repository;
    }

    // ✅ Add new item
    public WatchlistItem addItem(String userId, String type, String value) {
        WatchlistItem item = new WatchlistItem();
        item.setUserId(userId);
        item.setType(type.toLowerCase());
        item.setValue(value.toLowerCase());
        item.setCreatedAt(Instant.now());
        item.setLastCheckedAt(null);
        item.setActive(true);
        return repository.save(item);
    }

    // ✅ Check if an item exists for a user
    public boolean isItemWatched(String userId, String type, String value) {
        Optional<WatchlistItem> item = repository.findByUserIdAndTypeAndValue(
                userId,
                type.toLowerCase(),
                value.toLowerCase()
        );
        return item.isPresent() && item.get().isActive();
    }

    // ✅ Find all items for a user
    public List<WatchlistItem> findByUser(String userId) {
        return repository.findByUserId(userId);
    }

    // ✅ Remove item by ID
    public void removeItem(String id) {
        repository.deleteById(id);
    }

    // ✅ General-purpose lookup (manual type)
    public List<WatchlistItem> findByTypeAndValue(String type, String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return repository.findByTypeAndValue(type.toLowerCase(), value.toLowerCase());
    }

    // ✅ Convenience wrapper (default = email match)
    public List<WatchlistItem> findMatches(String value) {
        return findByTypeAndValue("email", value);
    }
}
