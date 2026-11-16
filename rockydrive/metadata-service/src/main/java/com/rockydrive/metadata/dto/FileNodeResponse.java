package com.rockydrive.metadata.dto;

import com.rockydrive.metadata.entity.FileNode;
import com.rockydrive.metadata.entity.FileType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record FileNodeResponse(
        String id,
        String parentId,
        String ownerId,
        String name,
        FileType type,
        long revision,
        long sizeBytes,
        String checksum,
        List<String> chunkIds,
        Instant createdAt,
        Instant updatedAt,
        List<FileNodeResponse> children
) {
    public static FileNodeResponse from(FileNode node) {
        return from(node, null);
    }

    public static FileNodeResponse from(FileNode node, List<FileNodeResponse> children) {
        return new FileNodeResponse(
                node.getId(), node.getParentId(), node.getOwnerId(), node.getName(), node.getType(),
                node.getRevision(), node.getSizeBytes(), node.getChecksum(), node.getChunkIds(),
                node.getCreatedAt(), node.getUpdatedAt(),
                children == null ? new ArrayList<>() : children);
    }
}
