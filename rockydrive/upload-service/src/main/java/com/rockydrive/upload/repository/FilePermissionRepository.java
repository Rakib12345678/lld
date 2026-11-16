package com.rockydrive.upload.repository;

import com.rockydrive.upload.model.FilePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilePermissionRepository extends JpaRepository<FilePermission, FilePermission.Key> {

    Optional<FilePermission> findByFileIdAndUserId(String fileId, String userId);
}
