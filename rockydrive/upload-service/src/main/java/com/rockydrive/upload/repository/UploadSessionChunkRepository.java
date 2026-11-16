package com.rockydrive.upload.repository;

import com.rockydrive.upload.model.UploadSessionChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UploadSessionChunkRepository extends JpaRepository<UploadSessionChunk, Long> {

    List<UploadSessionChunk> findBySessionId(String sessionId);

    Optional<UploadSessionChunk> findBySessionIdAndChunkIndex(String sessionId, int chunkIndex);
}
