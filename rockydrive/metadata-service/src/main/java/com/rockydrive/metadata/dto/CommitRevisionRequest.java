package com.rockydrive.metadata.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record CommitRevisionRequest(
        @NotNull @PositiveOrZero Long expectedRevision,
        @NotNull @PositiveOrZero Long newSizeBytes,
        String newChecksum,
        @NotEmpty List<String> chunkIds
) {
}
