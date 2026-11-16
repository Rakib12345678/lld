package com.rockydrive.upload.service;

import com.rockydrive.common.event.FileUploadedEvent;
import com.rockydrive.upload.dto.FinalizeUploadResponse;
import com.rockydrive.upload.messaging.UploadEventPublisher;
import com.rockydrive.upload.model.UploadSession;
import com.rockydrive.upload.model.UploadSessionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Finalize verifies every expected chunk actually committed, then hands off to metadata-service
 * asynchronously (Kafka event) instead of a synchronous commit call — see {@link UploadEventPublisher}.
 */
@Service
public class UploadFinalizationService {

    private final UploadSessionService uploadSessionService;
    private final UploadEventPublisher uploadEventPublisher;

    public UploadFinalizationService(UploadSessionService uploadSessionService,
                                      UploadEventPublisher uploadEventPublisher) {
        this.uploadSessionService = uploadSessionService;
        this.uploadEventPublisher = uploadEventPublisher;
    }

    @Transactional
    public FinalizeUploadResponse finalizeUpload(String sessionId) {
        if (!uploadSessionService.isComplete(sessionId)) {
            throw new IllegalStateException(
                    "Upload session %s still has pending chunks — cannot finalize".formatted(sessionId));
        }

        UploadSession session = uploadSessionService.getSessionOrThrow(sessionId);

        // TODO: real revision numbering needs the current revision from metadata-service's
        // denormalized cache (not yet built) rather than always starting at 1.
        long revision = 1L;
        String fileId = session.getFileId() != null ? session.getFileId() : UUID.randomUUID().toString();

        session.setFileId(fileId);
        session.setStatus(UploadSessionStatus.FINALIZED);

        uploadEventPublisher.publishFileUploaded(new FileUploadedEvent(
                fileId,
                session.getOwnerId(),
                revision,
                Instant.now(),
                session.getFileName(),
                session.getTotalSizeBytes(),
                null,
                session.getExpectedChunkCount()
        ));

        return new FinalizeUploadResponse(fileId, revision);
    }
}
