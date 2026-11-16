package com.rockydrive.metadata.repository;

import com.rockydrive.metadata.entity.FileShare;
import com.rockydrive.metadata.entity.SharePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, String> {

    List<FileShare> findByFileId(String fileId);

    Optional<FileShare> findByFileIdAndSharedWithUserId(String fileId, String sharedWithUserId);

    boolean existsByFileIdAndSharedWithUserIdAndPermissionIn(
            String fileId, String sharedWithUserId, Collection<SharePermission> permissions);
}
