package com.rockydrive.upload.service;

import com.rockydrive.upload.model.FilePermission;
import com.rockydrive.upload.model.PermissionRole;
import com.rockydrive.upload.repository.FilePermissionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Authorization checks against the denormalized {@link FilePermission} cache — never a synchronous
 * call to metadata-service. A missing entry means "not authorized", not "unknown" (fail closed).
 */
@Service
public class AuthorizationService {

    private final FilePermissionRepository filePermissionRepository;

    public AuthorizationService(FilePermissionRepository filePermissionRepository) {
        this.filePermissionRepository = filePermissionRepository;
    }

    /** True only if {@code userId} has OWNER or EDITOR access to {@code fileId} and it hasn't expired. */
    public boolean canUpload(String userId, String fileId) {
        return filePermissionRepository.findByFileIdAndUserId(fileId, userId)
                .filter(permission -> !permission.isExpired(Instant.now()))
                .map(FilePermission::getRole)
                .map(role -> role == PermissionRole.OWNER || role == PermissionRole.EDITOR)
                .orElse(false);
    }
}
