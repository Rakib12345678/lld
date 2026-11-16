package com.rockydrive.sync.dto;

import java.util.List;

public record DeltaResponse(
        List<ChangeEntryDTO> changes,
        long nextCursor
) {
}
