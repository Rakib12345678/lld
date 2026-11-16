package com.rockydrive.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Published when a brand-new file has finished uploading (all chunks received and assembled).
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param fileId      id of the file that was uploaded
 * @param actorId     id of the user who performed the upload
 * @param revision    revision number of the file after this event
 * @param timestamp   time the event was produced
 * @param fileName    original file name as provided by the client
 * @param sizeBytes   total size of the uploaded file, in bytes
 * @param contentType MIME type of the uploaded file, if known
 * @param chunkCount  number of chunks the file was split into
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileUploadedEvent(
        @JsonProperty("fileId") String fileId,
        @JsonProperty("actorId") String actorId,
        @JsonProperty("revision") long revision,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("sizeBytes") long sizeBytes,
        @JsonProperty("contentType") String contentType,
        @JsonProperty("chunkCount") int chunkCount
) implements FileEvent {

    @JsonCreator
    public FileUploadedEvent {
    }
}
