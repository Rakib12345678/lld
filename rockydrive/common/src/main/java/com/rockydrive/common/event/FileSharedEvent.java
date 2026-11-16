package com.rockydrive.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Published when a file is shared with another user.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param fileId           id of the file that was shared
 * @param actorId          id of the user who performed the share (the sharer/owner)
 * @param revision         revision number of the file at the time of sharing
 * @param timestamp        time the event was produced
 * @param sharedWithUserId id of the user the file was shared with
 * @param permission       access level granted, e.g. "READ" or "WRITE"
 * @param expiresAt        time the share expires and access should be revoked; null if the share
 *                         does not expire
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileSharedEvent(
        @JsonProperty("fileId") String fileId,
        @JsonProperty("actorId") String actorId,
        @JsonProperty("revision") long revision,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("sharedWithUserId") String sharedWithUserId,
        @JsonProperty("permission") String permission,
        @JsonProperty("expiresAt") Instant expiresAt
) implements FileEvent {

    @JsonCreator
    public FileSharedEvent {
    }
}
