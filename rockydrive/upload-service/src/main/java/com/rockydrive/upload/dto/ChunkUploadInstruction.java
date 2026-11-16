package com.rockydrive.upload.dto;

/**
 * One per requested chunk hash. {@code needsUpload = false} means the chunk was already found in
 * the dedup registry (a "copy" server-side, not re-transferred), so {@code presignedUrl} is null.
 */
public record ChunkUploadInstruction(
        int chunkIndex,
        String chunkHash,
        boolean needsUpload,
        String presignedUrl
) {
}
