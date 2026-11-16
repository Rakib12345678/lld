package com.rockydrive.upload.messaging;

import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.KafkaTopics;
import com.rockydrive.upload.model.FilePermission;
import com.rockydrive.upload.model.PermissionRole;
import com.rockydrive.upload.repository.FilePermissionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Keeps the local {@link FilePermission} cache in sync so presign requests can authorize without
 * a synchronous call to metadata-service.
 */
@Component
public class FileSharedEventListener {

    private final FilePermissionRepository filePermissionRepository;

    public FileSharedEventListener(FilePermissionRepository filePermissionRepository) {
        this.filePermissionRepository = filePermissionRepository;
    }

    @KafkaListener(topics = KafkaTopics.FILE_SHARED, groupId = "upload-service")
    public void onFileShared(FileSharedEvent event) {
        PermissionRole role = "WRITE".equalsIgnoreCase(event.permission())
                ? PermissionRole.EDITOR
                : PermissionRole.VIEWER;

        filePermissionRepository.save(new FilePermission(
                event.fileId(), event.sharedWithUserId(), role, event.expiresAt()));
    }
}
