package com.rockydrive.upload.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.IdClass;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Denormalized cache of "who can do what to which file", kept in sync by consuming
 * {@code FileSharedEvent} (and file-lifecycle events) from metadata-service over Kafka, rather
 * than calling metadata-service synchronously on every presign request.
 */
@Entity
@Table(name = "file_permission")
@IdClass(FilePermission.Key.class)
public class FilePermission {

    @Id
    private String fileId;

    @Id
    private String userId;

    @Enumerated(EnumType.STRING)
    private PermissionRole role;

    private Instant expiresAt;

    protected FilePermission() {
        // JPA
    }

    public FilePermission(String fileId, String userId, PermissionRole role, Instant expiresAt) {
        this.fileId = fileId;
        this.userId = userId;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public String getFileId() {
        return fileId;
    }

    public String getUserId() {
        return userId;
    }

    public PermissionRole getRole() {
        return role;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public record Key(String fileId, String userId) implements Serializable {
        public Key() {
            this(null, null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilePermission that)) return false;
        return Objects.equals(fileId, that.fileId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, userId);
    }
}
