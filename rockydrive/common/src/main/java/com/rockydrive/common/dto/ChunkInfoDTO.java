package com.rockydrive.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Shared representation of a single chunk of a file, as produced by chunked upload and consumed
 * by sync/metadata services.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param chunkId         unique id of the chunk
 * @param fileId          id of the file this chunk belongs to
 * @param chunkIndex      zero-based position of this chunk within the file
 * @param sizeBytes       size of this chunk, in bytes
 * @param checksum        checksum of this chunk's content, used for integrity verification
 * @param storageLocation opaque location/reference where the chunk bytes are stored
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChunkInfoDTO(
        @JsonProperty("chunkId") String chunkId,
        @JsonProperty("fileId") String fileId,
        @JsonProperty("chunkIndex") int chunkIndex,
        @JsonProperty("sizeBytes") long sizeBytes,
        @JsonProperty("checksum") String checksum,
        @JsonProperty("storageLocation") String storageLocation
) {

    @JsonCreator
    public ChunkInfoDTO {
    }
}
