package com.rockydrive.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Shared representation of a file's metadata, used across services that need to describe a file
 * without owning its storage details.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param fileId      unique id of the file
 * @param ownerId     id of the user who owns the file
 * @param fileName    current file name
 * @param sizeBytes   total size of the file, in bytes
 * @param contentType MIME type of the file, if known
 * @param checksum    checksum of the full file content
 * @param revision    current revision number of the file
 * @param chunkIds    ids of the chunks that make up the file, in order
 * @param createdAt   time the file was first created
 * @param updatedAt   time the file was last updated
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileMetadataDTO(
        @JsonProperty("fileId") String fileId,
        @JsonProperty("ownerId") String ownerId,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("sizeBytes") long sizeBytes,
        @JsonProperty("contentType") String contentType,
        @JsonProperty("checksum") String checksum,
        @JsonProperty("revision") long revision,
        @JsonProperty("chunkIds") List<String> chunkIds,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("updatedAt") Instant updatedAt
) {

    @JsonCreator
    public FileMetadataDTO {
    }
}
