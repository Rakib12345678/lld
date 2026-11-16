package com.rockydrive.metadata.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(
        @NotBlank String newName
) {
}
