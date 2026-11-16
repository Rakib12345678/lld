package com.rockydrive.upload.dto;

import com.rockydrive.upload.model.UploadSessionStatus;

import java.util.List;

/** Lets a client resume after a crash/network drop: which chunk indexes are still pending. */
public record UploadSessionStatusResponse(
        String sessionId,
        UploadSessionStatus status,
        int expectedChunkCount,
        List<Integer> pendingChunkIndexes
) {
}
