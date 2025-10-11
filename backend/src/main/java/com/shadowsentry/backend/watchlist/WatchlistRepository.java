package com.shadowsentry.backend.watchlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Watchlist items.
 * Provides lookup by userId, type, and value.
 */
@Repository
public interface WatchlistRepository extends MongoRepository<WatchlistItem, String> {

    /**
     * Find all watchlist items created by a user.
     */
    List<WatchlistItem> findByUserId(String userId);

    /**
     * Find a specific watchlist item by user, type, and value.
     */
    Optional<WatchlistItem> findByUserIdAndTypeAndValue(String userId, String type, String value);

    /**
     * Find all watchlist items by type and value across users.
     */
    List<WatchlistItem> findByTypeAndValue(String type, String value);
}
