package com.shadowsentry.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import com.shadowsentry.backend.model.RefreshToken;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoIndexes {

    private final MongoTemplate mongoTemplate;

    public MongoIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        // Ensure TTL index on expiresAt (expire after 0 seconds -> expire when field datetime passes)
        Index ttl = new Index().on("expiresAt", Sort.Direction.ASC).expire(0);
        mongoTemplate.indexOps(RefreshToken.class).createIndex(ttl);

        // Ensure index on userId for faster lookups
        Index userIdx = new Index().on("userId", Sort.Direction.ASC);
        mongoTemplate.indexOps(RefreshToken.class).createIndex(userIdx);
    }
}
