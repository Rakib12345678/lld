package com.rockydrive.upload.dto;

import java.util.List;

public record StartUploadSessionResponse(
        String sessionId,
        List<ChunkUploadInstruction> chunks
) {
}
