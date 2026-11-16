package com.rockydrive.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Published when the content of an existing file changes (new revision), e.g. after a sync or a
 * partial re-upload of changed chunks.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param fileId          id of the file that was updated
 * @param actorId         id of the user who performed the update
 * @param revision        new revision number of the file after this event
 * @param timestamp       time the event was produced
 * @param newSizeBytes    total size of the file after the update, in bytes
 * @param newChecksum     checksum of the file content after the update
 * @param updatedChunkIds ids of the chunks that changed as part of this update; may be empty if
 *                        the update only touched non-content fields
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileUpdatedEvent(
        @JsonProperty("fileId") String fileId,
        @JsonProperty("actorId") String actorId,
        @JsonProperty("revision") long revision,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("newSizeBytes") long newSizeBytes,
        @JsonProperty("newChecksum") String newChecksum,
        @JsonProperty("updatedChunkIds") List<String> updatedChunkIds
) implements FileEvent {

    @JsonCreator
    public FileUpdatedEvent {
    }
}
