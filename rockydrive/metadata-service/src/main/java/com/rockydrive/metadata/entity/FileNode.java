package com.rockydrive.metadata.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A file or folder in the tree, stored as an adjacency list (parentId -> children).
 * {@code revision} covers both content changes (commit) and structural changes
 * (rename/move) so that a single optimistic-concurrency check protects the whole node.
 */
@Entity
@Table(name = "file_node")
public class FileNode {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private FileType type;

    @Column(name = "revision", nullable = false)
    private long revision;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "checksum")
    private String checksum;

    @ElementCollection
    @CollectionTable(name = "file_node_chunk_ids", joinColumns = @JoinColumn(name = "file_node_id"))
    @OrderColumn(name = "chunk_order")
    @Column(name = "chunk_id")
    private List<String> chunkIds = new ArrayList<>();

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "conflict_source_id", length = 36)
    private String conflictSourceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FileNode() {
    }

    public FileNode(String id, String parentId, String ownerId, String name, FileType type, Instant now) {
        this.id = id;
        this.parentId = parentId;
        this.ownerId = ownerId;
        this.name = name;
        this.type = type;
        this.revision = 0;
        this.sizeBytes = 0;
        this.deleted = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileType getType() {
        return type;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public List<String> getChunkIds() {
        return chunkIds;
    }

    public void setChunkIds(List<String> chunkIds) {
        // Hibernate mutates this list in place when merging the collection - it must not be
        // an immutable List (e.g. List.of(...)).
        this.chunkIds = new ArrayList<>(chunkIds);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getConflictSourceId() {
        return conflictSourceId;
    }

    public void setConflictSourceId(String conflictSourceId) {
        this.conflictSourceId = conflictSourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
