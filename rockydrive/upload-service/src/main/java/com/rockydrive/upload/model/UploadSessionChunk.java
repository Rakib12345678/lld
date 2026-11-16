package com.rockydrive.upload.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "upload_session_chunk")
public class UploadSessionChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private int chunkIndex;
    private String chunkHash;
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    private ChunkUploadStatus status;

    /** Object key in the chunk store; set once the chunk is confirmed uploaded (or reused via dedup). */
    private String storageKey;

    protected UploadSessionChunk() {
        // JPA
    }

    public UploadSessionChunk(String sessionId, int chunkIndex, String chunkHash, long sizeBytes,
                               ChunkUploadStatus status, String storageKey) {
        this.sessionId = sessionId;
        this.chunkIndex = chunkIndex;
        this.chunkHash = chunkHash;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.storageKey = storageKey;
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getChunkHash() {
        return chunkHash;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public ChunkUploadStatus getStatus() {
        return status;
    }

    public void markUploaded(String storageKey) {
        this.status = ChunkUploadStatus.UPLOADED;
        this.storageKey = storageKey;
    }

    public String getStorageKey() {
        return storageKey;
    }
}
