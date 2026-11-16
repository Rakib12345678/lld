package com.rockydrive.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Published when a file is deleted, either moved to trash (soft delete) or permanently removed.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param fileId      id of the file that was deleted
 * @param actorId     id of the user who performed the deletion
 * @param revision    revision number of the file at the time of deletion
 * @param timestamp   time the event was produced
 * @param hardDelete  true if the file (and its chunks) were permanently removed; false if this
 *                    was a soft/trash delete that could still be restored
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileDeletedEvent(
        @JsonProperty("fileId") String fileId,
        @JsonProperty("actorId") String actorId,
        @JsonProperty("revision") long revision,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("hardDelete") boolean hardDelete
) implements FileEvent {

    @JsonCreator
    public FileDeletedEvent {
    }
}
