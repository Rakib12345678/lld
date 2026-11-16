package com.rockydrive.upload.dto;

import jakarta.validation.constraints.NotBlank;

/** Sent by the client after a successful PUT to the presigned URL, to mark that chunk committed. */
public record CommitChunkRequest(@NotBlank String storageKey) {
}
