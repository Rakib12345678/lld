package com.rockydrive.sync.dto;

import com.rockydrive.sync.model.ChangeType;

import java.time.Instant;

/**
 * One entry in a /delta response. {@code payload} is the raw JSON of the originating common
 * event (FileUploadedEvent/FileUpdatedEvent/FileDeletedEvent/FileSharedEvent) - clients switch
 * on changeType to know how to parse it, so sync-service doesn't need to redeclare every field.
 */
public record ChangeEntryDTO(
        String fileId,
        ChangeType changeType,
        long revision,
        Instant occurredAt,
        String payload
) {
}
