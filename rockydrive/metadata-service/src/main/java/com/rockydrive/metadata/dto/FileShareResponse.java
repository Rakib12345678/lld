package com.rockydrive.metadata.dto;

import com.rockydrive.metadata.entity.FileShare;
import com.rockydrive.metadata.entity.SharePermission;

import java.time.Instant;

public record FileShareResponse(
        String id,
        String fileId,
        String sharedWithUserId,
        SharePermission permission,
        Instant sharedAt
) {
    public static FileShareResponse from(FileShare share) {
        return new FileShareResponse(
                share.getId(), share.getFileId(), share.getSharedWithUserId(),
                share.getPermission(), share.getSharedAt());
    }
}
