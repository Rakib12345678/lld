package com.rockydrive.sync.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Read-optimized projection built from file.* Kafka events - never joins against
 * metadata-service's own tables. One row per (user, file change). Indexed on
 * (user_id, revision) since the /delta read pattern is high-frequency range scans:
 * "give me every change for this user with revision > cursor".
 */
@Entity
@Table(
        name = "changelog",
        indexes = @Index(name = "idx_changelog_user_revision", columnList = "user_id, revision")
)
public class ChangeLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "revision", nullable = false)
    private long revision;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** Raw JSON of the source event (FileUploadedEvent/FileUpdatedEvent/...), kept as-is so the
     * delta response can pass it straight through without redefining every event's fields here. */
    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    protected ChangeLogEntry() {
    }

    public ChangeLogEntry(String userId, String fileId, ChangeType changeType, long revision,
                           Instant occurredAt, String payload) {
        this.userId = userId;
        this.fileId = fileId;
        this.changeType = changeType;
        this.revision = revision;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileId() {
        return fileId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public long getRevision() {
        return revision;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getPayload() {
        return payload;
    }
}
