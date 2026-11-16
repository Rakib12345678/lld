package com.rockydrive.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Either {@code fileId} (new revision of an existing file) or {@code folderId} (brand-new file)
 * must be set — validated in the service layer, not here, since it's a cross-field rule.
 */
public record StartUploadSessionRequest(
        String fileId,
        String folderId,
        @NotBlank String fileName,
        @Positive long totalSizeBytes,
        @NotEmpty List<String> chunkHashes
) {
}
