package com.rockydrive.metadata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "file_share",
        uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "shared_with_user_id"})
)
public class FileShare {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "file_id", nullable = false, length = 36)
    private String fileId;

    @Column(name = "shared_with_user_id", nullable = false, length = 64)
    private String sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 16)
    private SharePermission permission;

    @Column(name = "shared_at", nullable = false)
    private Instant sharedAt;

    protected FileShare() {
    }

    public FileShare(String id, String fileId, String sharedWithUserId, SharePermission permission, Instant sharedAt) {
        this.id = id;
        this.fileId = fileId;
        this.sharedWithUserId = sharedWithUserId;
        this.permission = permission;
        this.sharedAt = sharedAt;
    }

    public String getId() {
        return id;
    }

    public String getFileId() {
        return fileId;
    }

    public String getSharedWithUserId() {
        return sharedWithUserId;
    }

    public SharePermission getPermission() {
        return permission;
    }

    public void setPermission(SharePermission permission) {
        this.permission = permission;
    }

    public Instant getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Instant sharedAt) {
        this.sharedAt = sharedAt;
    }
}
