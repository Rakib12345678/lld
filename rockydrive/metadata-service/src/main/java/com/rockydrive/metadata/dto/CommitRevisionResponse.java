package com.rockydrive.metadata.dto;

public record CommitRevisionResponse(
        String fileId,
        long revision
) {
}
