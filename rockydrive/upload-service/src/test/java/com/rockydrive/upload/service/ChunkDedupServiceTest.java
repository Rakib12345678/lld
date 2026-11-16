package com.rockydrive.upload.service;

import com.rockydrive.upload.repository.ChunkRegistryRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChunkDedupServiceTest {

    private final ChunkRegistryRepository chunkRegistryRepository = mock(ChunkRegistryRepository.class);
    private final ChunkDedupService chunkDedupService = new ChunkDedupService(chunkRegistryRepository);

    @Test
    void reportsExistingChunkAsAlreadyStored() {
        when(chunkRegistryRepository.findStorageKey("hash-a")).thenReturn(Optional.of("chunks/xyz/hash-a"));

        Optional<String> result = chunkDedupService.findExistingStorageKey("hash-a");

        assertThat(result).contains("chunks/xyz/hash-a");
    }

    @Test
    void reportsUnknownChunkAsMissing() {
        when(chunkRegistryRepository.findStorageKey("hash-b")).thenReturn(Optional.empty());

        Optional<String> result = chunkDedupService.findExistingStorageKey("hash-b");

        assertThat(result).isEmpty();
    }

    @Test
    void registeringAStoredChunkWritesThroughToTheRegistry() {
        chunkDedupService.registerStored("hash-c", "chunks/xyz/hash-c");

        verify(chunkRegistryRepository).register("hash-c", "chunks/xyz/hash-c");
    }
}
