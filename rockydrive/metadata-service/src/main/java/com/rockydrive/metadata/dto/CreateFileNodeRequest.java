package com.rockydrive.metadata.dto;

import com.rockydrive.metadata.entity.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFileNodeRequest(
        String parentId,
        @NotBlank String name,
        @NotNull FileType type
) {
}
