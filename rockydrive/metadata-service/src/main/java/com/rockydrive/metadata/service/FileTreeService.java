package com.rockydrive.metadata.service;

import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.metadata.entity.FileNode;
import com.rockydrive.metadata.entity.FileType;
import com.rockydrive.metadata.entity.SharePermission;
import com.rockydrive.metadata.event.FileEventPublisher;
import com.rockydrive.metadata.exception.FileNotFoundException;
import com.rockydrive.metadata.exception.ForbiddenException;
import com.rockydrive.metadata.exception.StaleRevisionException;
import com.rockydrive.metadata.repository.FileNodeRepository;
import com.rockydrive.metadata.repository.FileShareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileTreeService {

    private static final Set<SharePermission> WRITE_PERMISSIONS = Set.of(SharePermission.EDITOR, SharePermission.OWNER);

    private final FileNodeRepository fileNodeRepository;
    private final FileShareRepository fileShareRepository;
    private final FileEventPublisher eventPublisher;

    public FileTreeService(
            FileNodeRepository fileNodeRepository,
            FileShareRepository fileShareRepository,
            FileEventPublisher eventPublisher) {
        this.fileNodeRepository = fileNodeRepository;
        this.fileShareRepository = fileShareRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FileNode create(String parentId, String name, FileType type, String actorId) {
        if (parentId != null) {
            FileNode parent = getActive(parentId);
            if (parent.getType() != FileType.FOLDER) {
                throw new IllegalArgumentException("Parent " + parentId + " is not a folder");
            }
            requireWriteAccess(parent, actorId);
        }
        FileNode node = new FileNode(UUID.randomUUID().toString(), parentId, actorId, name, type, Instant.now());
        return fileNodeRepository.save(node);
    }

    @Transactional
    public FileNode rename(String fileId, String newName, String actorId) {
        FileNode node = lockActive(fileId);
        requireWriteAccess(node, actorId);
        node.setName(newName);
        return bumpAndPublish(node, actorId, List.of());
    }

    @Transactional
    public FileNode move(String fileId, String newParentId, String actorId) {
        FileNode node = lockActive(fileId);
        requireWriteAccess(node, actorId);

        if (newParentId != null) {
            FileNode newParent = getActive(newParentId);
            if (newParent.getType() != FileType.FOLDER) {
                throw new IllegalArgumentException("Target parent " + newParentId + " is not a folder");
            }
            requireWriteAccess(newParent, actorId);
            if (newParentId.equals(fileId) || isDescendant(newParentId, fileId)) {
                throw new IllegalArgumentException("Cannot move a folder into its own subtree");
            }
        }

        node.setParentId(newParentId);
        return bumpAndPublish(node, actorId, List.of());
    }

    @Transactional(noRollbackFor = StaleRevisionException.class)
    public CommitResult commitRevision(
            String fileId, long expectedRevision, long newSizeBytes, String newChecksum,
            List<String> chunkIds, String actorId) {
        FileNode node = lockActive(fileId);
        if (node.getType() != FileType.FILE) {
            throw new IllegalArgumentException("Cannot commit a revision to a folder: " + fileId);
        }
        requireWriteAccess(node, actorId);

        if (node.getRevision() != expectedRevision) {
            FileNode conflictedCopy = createConflictedCopy(node, newSizeBytes, newChecksum, chunkIds, actorId);
            throw new StaleRevisionException(fileId, node.getRevision(), expectedRevision, conflictedCopy.getId());
        }

        node.setSizeBytes(newSizeBytes);
        node.setChecksum(newChecksum);
        node.setChunkIds(chunkIds);
        FileNode saved = bumpAndPublish(node, actorId, chunkIds);
        return new CommitResult(saved.getId(), saved.getRevision());
    }

    @Transactional
    public void delete(String fileId, String actorId, boolean hardDelete) {
        FileNode node = lockActive(fileId);
        requireWriteAccess(node, actorId);

        if (node.getType() == FileType.FOLDER) {
            for (FileNode child : fileNodeRepository.findByParentIdAndDeletedFalse(fileId)) {
                delete(child.getId(), actorId, hardDelete);
            }
        }

        long revisionAtDeletion = node.getRevision();
        if (hardDelete) {
            fileNodeRepository.delete(node);
        } else {
            node.setDeleted(true);
            node.setUpdatedAt(Instant.now());
            fileNodeRepository.save(node);
        }

        eventPublisher.publishFileDeleted(
                new FileDeletedEvent(fileId, actorId, revisionAtDeletion, Instant.now(), hardDelete));
    }

    @Transactional(readOnly = true)
    public FileNode getActive(String fileId) {
        FileNode node = fileNodeRepository.findById(fileId).orElseThrow(() -> new FileNotFoundException(fileId));
        if (node.isDeleted()) {
            throw new FileNotFoundException(fileId);
        }
        return node;
    }

    @Transactional(readOnly = true)
    public List<FileNode> listChildren(String parentId, String ownerId) {
        return parentId == null
                ? fileNodeRepository.findByOwnerIdAndParentIdIsNullAndDeletedFalse(ownerId)
                : fileNodeRepository.findByParentIdAndDeletedFalse(parentId);
    }

    boolean hasWriteAccess(FileNode node, String actorId) {
        return node.getOwnerId().equals(actorId)
                || fileShareRepository.existsByFileIdAndSharedWithUserIdAndPermissionIn(
                        node.getId(), actorId, WRITE_PERMISSIONS);
    }

    private void requireWriteAccess(FileNode node, String actorId) {
        if (!hasWriteAccess(node, actorId)) {
            throw new ForbiddenException("User " + actorId + " does not have write access to " + node.getId());
        }
    }

    private FileNode lockActive(String fileId) {
        FileNode node = fileNodeRepository.findByIdForUpdate(fileId).orElseThrow(() -> new FileNotFoundException(fileId));
        if (node.isDeleted()) {
            throw new FileNotFoundException(fileId);
        }
        return node;
    }

    private boolean isDescendant(String candidateId, String ancestorId) {
        Deque<String> stack = new ArrayDeque<>();
        stack.push(ancestorId);
        while (!stack.isEmpty()) {
            String currentId = stack.pop();
            if (currentId.equals(candidateId)) {
                return true;
            }
            for (FileNode child : fileNodeRepository.findByParentIdAndDeletedFalse(currentId)) {
                stack.push(child.getId());
            }
        }
        return false;
    }

    private FileNode createConflictedCopy(
            FileNode source, long sizeBytes, String checksum, List<String> chunkIds, String actorId) {
        FileNode copy = new FileNode(
                UUID.randomUUID().toString(), source.getParentId(), actorId,
                conflictedCopyName(source.getName()), FileType.FILE, Instant.now());
        copy.setRevision(1);
        copy.setSizeBytes(sizeBytes);
        copy.setChecksum(checksum);
        copy.setChunkIds(chunkIds);
        copy.setConflictSourceId(source.getId());
        return fileNodeRepository.save(copy);
    }

    private String conflictedCopyName(String originalName) {
        String suffix = " (conflicted copy " + Instant.now() + ")";
        int dot = originalName.lastIndexOf('.');
        if (dot <= 0) {
            return originalName + suffix;
        }
        return originalName.substring(0, dot) + suffix + originalName.substring(dot);
    }

    private FileNode bumpAndPublish(FileNode node, String actorId, List<String> updatedChunkIds) {
        node.setRevision(node.getRevision() + 1);
        Instant now = Instant.now();
        node.setUpdatedAt(now);
        FileNode saved = fileNodeRepository.save(node);

        eventPublisher.publishFileUpdated(new FileUpdatedEvent(
                saved.getId(), actorId, saved.getRevision(), now,
                saved.getSizeBytes(), saved.getChecksum(), updatedChunkIds));
        return saved;
    }

    public record CommitResult(String fileId, long revision) {
    }
}
