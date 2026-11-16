package com.rockydrive.upload.service;

import com.rockydrive.upload.dto.ChunkUploadInstruction;
import com.rockydrive.upload.dto.StartUploadSessionRequest;
import com.rockydrive.upload.dto.StartUploadSessionResponse;
import com.rockydrive.upload.dto.UploadSessionStatusResponse;
import com.rockydrive.upload.model.ChunkUploadStatus;
import com.rockydrive.upload.model.UploadSession;
import com.rockydrive.upload.model.UploadSessionChunk;
import com.rockydrive.upload.repository.UploadSessionChunkRepository;
import com.rockydrive.upload.repository.UploadSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns upload session lifecycle and per-chunk commit state so a client can resume an interrupted
 * upload: re-fetch the session status and find out exactly which chunk indexes are still pending.
 */
@Service
public class UploadSessionService {

    private final UploadSessionRepository uploadSessionRepository;
    private final UploadSessionChunkRepository chunkRepository;
    private final ChunkDedupService chunkDedupService;
    private final PresignedUrlService presignedUrlService;

    public UploadSessionService(UploadSessionRepository uploadSessionRepository,
                                 UploadSessionChunkRepository chunkRepository,
                                 ChunkDedupService chunkDedupService,
                                 PresignedUrlService presignedUrlService) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.chunkRepository = chunkRepository;
        this.chunkDedupService = chunkDedupService;
        this.presignedUrlService = presignedUrlService;
    }

    @Transactional
    public StartUploadSessionResponse startSession(StartUploadSessionRequest request, String ownerId) {
        String sessionId = UUID.randomUUID().toString();
        List<String> chunkHashes = request.chunkHashes();

        UploadSession session = new UploadSession(sessionId, request.fileId(), request.folderId(), ownerId,
                request.fileName(), request.totalSizeBytes(), chunkHashes.size());
        uploadSessionRepository.save(session);

        List<ChunkUploadInstruction> instructions = new ArrayList<>(chunkHashes.size());
        for (int i = 0; i < chunkHashes.size(); i++) {
            String hash = chunkHashes.get(i);
            Optional<String> existingKey = chunkDedupService.findExistingStorageKey(hash);

            if (existingKey.isPresent()) {
                chunkRepository.save(new UploadSessionChunk(
                        sessionId, i, hash, 0, ChunkUploadStatus.UPLOADED, existingKey.get()));
                instructions.add(new ChunkUploadInstruction(i, hash, false, null));
            } else {
                String targetKey = presignedUrlService.storageKeyFor(sessionId, hash);
                chunkRepository.save(new UploadSessionChunk(
                        sessionId, i, hash, 0, ChunkUploadStatus.PENDING, targetKey));
                String presignedUrl = presignedUrlService.presignChunkUpload(targetKey);
                instructions.add(new ChunkUploadInstruction(i, hash, true, presignedUrl));
            }
        }

        return new StartUploadSessionResponse(sessionId, instructions);
    }

    @Transactional
    public void commitChunk(String sessionId, int chunkIndex, String storageKey) {
        UploadSessionChunk chunk = chunkRepository.findBySessionIdAndChunkIndex(sessionId, chunkIndex)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No chunk %d in session %s".formatted(chunkIndex, sessionId)));

        chunk.markUploaded(storageKey);
        chunkRepository.save(chunk);
        chunkDedupService.registerStored(chunk.getChunkHash(), storageKey);
    }

    public UploadSessionStatusResponse getStatus(String sessionId) {
        UploadSession session = getSessionOrThrow(sessionId);
        List<Integer> pending = chunkRepository.findBySessionId(sessionId).stream()
                .filter(chunk -> chunk.getStatus() != ChunkUploadStatus.UPLOADED)
                .map(UploadSessionChunk::getChunkIndex)
                .sorted()
                .toList();

        return new UploadSessionStatusResponse(sessionId, session.getStatus(), session.getExpectedChunkCount(), pending);
    }

    public boolean isComplete(String sessionId) {
        List<UploadSessionChunk> chunks = chunkRepository.findBySessionId(sessionId);
        UploadSession session = getSessionOrThrow(sessionId);
        return chunks.size() == session.getExpectedChunkCount()
                && chunks.stream().allMatch(chunk -> chunk.getStatus() == ChunkUploadStatus.UPLOADED);
    }

    public UploadSession getSessionOrThrow(String sessionId) {
        return uploadSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("No upload session " + sessionId));
    }
}
