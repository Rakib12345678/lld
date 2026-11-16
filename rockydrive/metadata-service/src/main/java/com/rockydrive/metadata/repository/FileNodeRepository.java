package com.rockydrive.metadata.repository;

import com.rockydrive.metadata.entity.FileNode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileNodeRepository extends JpaRepository<FileNode, String> {

    List<FileNode> findByParentIdAndDeletedFalse(String parentId);

    List<FileNode> findByOwnerIdAndParentIdIsNullAndDeletedFalse(String ownerId);

    /**
     * Row-locks the node for the duration of the transaction so a concurrent commit can't slip
     * between the revision check and the write - the check-then-write must be atomic.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FileNode f where f.id = :id")
    Optional<FileNode> findByIdForUpdate(@Param("id") String id);
}
