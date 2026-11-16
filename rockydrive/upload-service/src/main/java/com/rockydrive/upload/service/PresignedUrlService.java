package com.rockydrive.upload.service;

import com.rockydrive.upload.config.StorageProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/** Issues presigned PUT URLs against MinIO/S3 for chunks that {@link ChunkDedupService} says are missing. */
@Service
public class PresignedUrlService {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(15);

    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;

    public PresignedUrlService(S3Presigner s3Presigner, StorageProperties storageProperties) {
        this.s3Presigner = s3Presigner;
        this.storageProperties = storageProperties;
    }

    /** @return a time-limited URL the client can PUT the raw chunk bytes to directly. */
    public String presignChunkUpload(String storageKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageProperties.bucket())
                .key(storageKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_TTL)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public String storageKeyFor(String sessionId, String chunkHash) {
        return "chunks/%s/%s".formatted(sessionId, chunkHash);
    }
}
