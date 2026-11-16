package com.rockydrive.metadata.controller;

import com.rockydrive.metadata.dto.CommitRevisionRequest;
import com.rockydrive.metadata.dto.CommitRevisionResponse;
import com.rockydrive.metadata.dto.CreateFileNodeRequest;
import com.rockydrive.metadata.dto.FileNodeResponse;
import com.rockydrive.metadata.dto.MoveRequest;
import com.rockydrive.metadata.dto.RenameRequest;
import com.rockydrive.metadata.entity.FileNode;
import com.rockydrive.metadata.security.JwtAuthFilter;
import com.rockydrive.metadata.service.FileTreeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileTreeService fileTreeService;

    public FileController(FileTreeService fileTreeService) {
        this.fileTreeService = fileTreeService;
    }

    @PostMapping
    public ResponseEntity<FileNodeResponse> create(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @Valid @RequestBody CreateFileNodeRequest request) {
        FileNode node = fileTreeService.create(request.parentId(), request.name(), request.type(), actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileNodeResponse.from(node));
    }

    @PatchMapping("/{fileId}/rename")
    public FileNodeResponse rename(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId,
            @Valid @RequestBody RenameRequest request) {
        return FileNodeResponse.from(fileTreeService.rename(fileId, request.newName(), actorId));
    }

    @PatchMapping("/{fileId}/move")
    public FileNodeResponse move(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId,
            @RequestBody MoveRequest request) {
        return FileNodeResponse.from(fileTreeService.move(fileId, request.newParentId(), actorId));
    }

    @PostMapping("/{fileId}/revisions")
    public CommitRevisionResponse commitRevision(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId,
            @Valid @RequestBody CommitRevisionRequest request) {
        var result = fileTreeService.commitRevision(
                fileId, request.expectedRevision(), request.newSizeBytes(),
                request.newChecksum(), request.chunkIds(), actorId);
        return new CommitRevisionResponse(result.fileId(), result.revision());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId,
            @RequestParam(defaultValue = "false") boolean hard) {
        fileTreeService.delete(fileId, actorId, hard);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fileId}")
    public FileNodeResponse get(@PathVariable String fileId) {
        return FileNodeResponse.from(fileTreeService.getActive(fileId));
    }

    @GetMapping("/tree")
    public List<FileNodeResponse> tree(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @RequestParam(required = false) String parentId) {
        return fileTreeService.listChildren(parentId, actorId).stream()
                .map(FileNodeResponse::from)
                .toList();
    }
}
