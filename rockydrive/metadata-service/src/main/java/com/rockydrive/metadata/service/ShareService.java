package com.rockydrive.metadata.service;

import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.metadata.entity.FileNode;
import com.rockydrive.metadata.entity.FileShare;
import com.rockydrive.metadata.entity.SharePermission;
import com.rockydrive.metadata.event.FileEventPublisher;
import com.rockydrive.metadata.exception.ForbiddenException;
import com.rockydrive.metadata.repository.FileShareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ShareService {

    private final FileShareRepository fileShareRepository;
    private final FileTreeService fileTreeService;
    private final FileEventPublisher eventPublisher;

    public ShareService(
            FileShareRepository fileShareRepository,
            FileTreeService fileTreeService,
            FileEventPublisher eventPublisher) {
        this.fileShareRepository = fileShareRepository;
        this.fileTreeService = fileTreeService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FileShare share(String fileId, String sharedWithUserId, SharePermission permission, String actorId) {
        FileNode node = fileTreeService.getActive(fileId);
        if (!node.getOwnerId().equals(actorId)) {
            throw new ForbiddenException("Only the owner of " + fileId + " can manage sharing");
        }
        if (sharedWithUserId.equals(node.getOwnerId())) {
            throw new IllegalArgumentException("Cannot share a file with its own owner");
        }

        FileShare share = fileShareRepository.findByFileIdAndSharedWithUserId(fileId, sharedWithUserId)
                .orElseGet(() -> new FileShare(UUID.randomUUID().toString(), fileId, sharedWithUserId, permission, Instant.now()));
        share.setPermission(permission);
        share.setSharedAt(Instant.now());
        FileShare saved = fileShareRepository.save(share);

        eventPublisher.publishFileShared(new FileSharedEvent(
                fileId, actorId, node.getRevision(), Instant.now(), sharedWithUserId, permission.name(), null));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FileShare> listShares(String fileId, String actorId) {
        FileNode node = fileTreeService.getActive(fileId);
        boolean canView = node.getOwnerId().equals(actorId) || fileTreeService.hasWriteAccess(node, actorId);
        if (!canView) {
            throw new ForbiddenException("User " + actorId + " cannot view shares for " + fileId);
        }
        return fileShareRepository.findByFileId(fileId);
    }
}
