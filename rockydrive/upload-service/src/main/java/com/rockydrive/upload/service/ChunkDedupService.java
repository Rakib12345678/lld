package com.rockydrive.upload.service;

import com.rockydrive.upload.repository.ChunkRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Tells the caller which chunk hashes are already stored somewhere (any file, any owner) so the
 * client never re-uploads bytes the server already has.
 */
@Service
public class ChunkDedupService {

    private final ChunkRegistryRepository chunkRegistryRepository;

    public ChunkDedupService(ChunkRegistryRepository chunkRegistryRepository) {
        this.chunkRegistryRepository = chunkRegistryRepository;
    }

    /** @return the existing storage key for this hash, or empty if the chunk needs to be uploaded. */
    public Optional<String> findExistingStorageKey(String chunkHash) {
        return chunkRegistryRepository.findStorageKey(chunkHash);
    }

    public boolean isAlreadyStored(String chunkHash) {
        return chunkRegistryRepository.exists(chunkHash);
    }

    public void registerStored(String chunkHash, String storageKey) {
        chunkRegistryRepository.register(chunkHash, storageKey);
    }
}
