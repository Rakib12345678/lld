package com.rockydrive.upload.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Global content-hash -> storage-key registry backing chunk dedup, i.e. "has any file, anywhere,
 * already stored a chunk with this hash". Backed by Redis for fast lookups on the hot upload path;
 * this is a cache/index in front of the source of truth (S3/MinIO), not the source of truth itself.
 */
@Repository
public class ChunkRegistryRepository {

    private static final String KEY_PREFIX = "chunk:registry:";

    private final StringRedisTemplate redisTemplate;

    public ChunkRegistryRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> findStorageKey(String chunkHash) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + chunkHash));
    }

    public boolean exists(String chunkHash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + chunkHash));
    }

    public void register(String chunkHash, String storageKey) {
        redisTemplate.opsForValue().set(KEY_PREFIX + chunkHash, storageKey);
    }
}
