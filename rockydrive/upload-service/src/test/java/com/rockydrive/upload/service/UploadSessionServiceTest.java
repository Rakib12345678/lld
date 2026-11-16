package com.rockydrive.upload.service;

import com.rockydrive.upload.dto.StartUploadSessionRequest;
import com.rockydrive.upload.dto.StartUploadSessionResponse;
import com.rockydrive.upload.dto.UploadSessionStatusResponse;
import com.rockydrive.upload.model.ChunkUploadStatus;
import com.rockydrive.upload.model.UploadSession;
import com.rockydrive.upload.model.UploadSessionChunk;
import com.rockydrive.upload.repository.UploadSessionChunkRepository;
import com.rockydrive.upload.repository.UploadSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the resumable-session contract: after a partial upload, {@code getStatus}/{@code
 * isComplete} must reflect exactly which chunks are still pending so a client can resume without
 * re-uploading chunks it already committed.
 */
class UploadSessionServiceTest {

    private final UploadSessionRepository uploadSessionRepository = mock(UploadSessionRepository.class);
    private final UploadSessionChunkRepository chunkRepository = mock(UploadSessionChunkRepository.class);
    private final ChunkDedupService chunkDedupService = mock(ChunkDedupService.class);
    private final PresignedUrlService presignedUrlService = mock(PresignedUrlService.class);

    private UploadSessionService uploadSessionService;

    @BeforeEach
    void setUp() {
        uploadSessionService = new UploadSessionService(
                uploadSessionRepository, chunkRepository, chunkDedupService, presignedUrlService);
    }

    @Test
    void startSessionSkipsPresignForChunksAlreadyInTheDedupRegistry() {
        when(chunkDedupService.findExistingStorageKey("dupe-hash")).thenReturn(Optional.of("chunks/existing/dupe-hash"));
        when(chunkDedupService.findExistingStorageKey("new-hash")).thenReturn(Optional.empty());
        when(presignedUrlService.storageKeyFor(anyString(), anyString())).thenReturn("chunks/session/new-hash");
        when(presignedUrlService.presignChunkUpload(anyString())).thenReturn("https://minio.local/presigned");

        StartUploadSessionRequest request = new StartUploadSessionRequest(
                null, "folder-1", "movie.mp4", 8_000_000, List.of("dupe-hash", "new-hash"));

        StartUploadSessionResponse response = uploadSessionService.startSession(request, "user-1");

        assertThat(response.chunks()).hasSize(2);
        assertThat(response.chunks().get(0).needsUpload()).isFalse();
        assertThat(response.chunks().get(0).presignedUrl()).isNull();
        assertThat(response.chunks().get(1).needsUpload()).isTrue();
        assertThat(response.chunks().get(1).presignedUrl()).isEqualTo("https://minio.local/presigned");
    }

    @Test
    void sessionIsIncompleteUntilEveryChunkIsCommitted() {
        String sessionId = "session-1";
        UploadSession session = new UploadSession(sessionId, null, "folder-1", "user-1", "movie.mp4", 16_000_000, 2);
        when(uploadSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        UploadSessionChunk pending = new UploadSessionChunk(sessionId, 0, "hash-0", 8_000_000, ChunkUploadStatus.PENDING, "chunks/session-1/hash-0");
        UploadSessionChunk uploaded = new UploadSessionChunk(sessionId, 1, "hash-1", 8_000_000, ChunkUploadStatus.UPLOADED, "chunks/session-1/hash-1");
        when(chunkRepository.findBySessionId(sessionId)).thenReturn(List.of(pending, uploaded));

        assertThat(uploadSessionService.isComplete(sessionId)).isFalse();

        UploadSessionStatusResponse status = uploadSessionService.getStatus(sessionId);
        assertThat(status.pendingChunkIndexes()).containsExactly(0);
    }

    @Test
    void sessionIsCompleteOnceEveryExpectedChunkIsUploaded() {
        String sessionId = "session-2";
        UploadSession session = new UploadSession(sessionId, null, "folder-1", "user-1", "movie.mp4", 16_000_000, 2);
        when(uploadSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        UploadSessionChunk chunk0 = new UploadSessionChunk(sessionId, 0, "hash-0", 8_000_000, ChunkUploadStatus.UPLOADED, "chunks/session-2/hash-0");
        UploadSessionChunk chunk1 = new UploadSessionChunk(sessionId, 1, "hash-1", 8_000_000, ChunkUploadStatus.UPLOADED, "chunks/session-2/hash-1");
        when(chunkRepository.findBySessionId(sessionId)).thenReturn(List.of(chunk0, chunk1));

        assertThat(uploadSessionService.isComplete(sessionId)).isTrue();
    }

    @Test
    void commitChunkMarksItUploadedAndRegistersItForDedup() {
        String sessionId = "session-3";
        UploadSessionChunk chunk = new UploadSessionChunk(sessionId, 0, "hash-0", 8_000_000, ChunkUploadStatus.PENDING, "chunks/session-3/hash-0");
        when(chunkRepository.findBySessionIdAndChunkIndex(sessionId, 0)).thenReturn(Optional.of(chunk));
        when(chunkRepository.save(any())).thenReturn(chunk);

        uploadSessionService.commitChunk(sessionId, 0, "chunks/session-3/hash-0");

        assertThat(chunk.getStatus()).isEqualTo(ChunkUploadStatus.UPLOADED);
    }
}
