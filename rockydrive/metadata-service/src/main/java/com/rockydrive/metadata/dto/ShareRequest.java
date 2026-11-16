package com.rockydrive.metadata.dto;

import com.rockydrive.metadata.entity.SharePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShareRequest(
        @NotBlank String sharedWithUserId,
        @NotNull SharePermission permission
) {
}
