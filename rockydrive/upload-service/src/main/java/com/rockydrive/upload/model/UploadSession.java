package com.rockydrive.upload.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks one in-flight (or completed) chunked upload so it can be resumed after a client crash or
 * network drop — the client re-fetches this session and its {@link UploadSessionChunk} rows to
 * find out which chunks it still needs to (re)send.
 */
@Entity
@Table(name = "upload_session")
public class UploadSession {

    @Id
    private String id;

    /** Target file id in metadata-service; null for a brand-new file until finalize assigns one. */
    private String fileId;

    /** Target folder for a brand-new file. */
    private String folderId;

    private String ownerId;
    private String fileName;
    private long totalSizeBytes;
    private int expectedChunkCount;

    @Enumerated(EnumType.STRING)
    private UploadSessionStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "sessionId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadSessionChunk> chunks = new ArrayList<>();

    protected UploadSession() {
        // JPA
    }

    public UploadSession(String id, String fileId, String folderId, String ownerId, String fileName,
                          long totalSizeBytes, int expectedChunkCount) {
        this.id = id;
        this.fileId = fileId;
        this.folderId = folderId;
        this.ownerId = ownerId;
        this.fileName = fileName;
        this.totalSizeBytes = totalSizeBytes;
        this.expectedChunkCount = expectedChunkCount;
        this.status = UploadSessionStatus.IN_PROGRESS;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public int getExpectedChunkCount() {
        return expectedChunkCount;
    }

    public UploadSessionStatus getStatus() {
        return status;
    }

    public void setStatus(UploadSessionStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<UploadSessionChunk> getChunks() {
        return chunks;
    }
}
