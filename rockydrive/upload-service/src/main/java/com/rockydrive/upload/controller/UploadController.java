package com.rockydrive.upload.controller;

import com.rockydrive.upload.dto.CommitChunkRequest;
import com.rockydrive.upload.dto.FinalizeUploadResponse;
import com.rockydrive.upload.dto.StartUploadSessionRequest;
import com.rockydrive.upload.dto.StartUploadSessionResponse;
import com.rockydrive.upload.dto.UploadSessionStatusResponse;
import com.rockydrive.upload.service.AuthorizationService;
import com.rockydrive.upload.service.JwtService;
import com.rockydrive.upload.service.UploadFinalizationService;
import com.rockydrive.upload.service.UploadSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/uploads/sessions")
public class UploadController {

    private final JwtService jwtService;
    private final AuthorizationService authorizationService;
    private final UploadSessionService uploadSessionService;
    private final UploadFinalizationService uploadFinalizationService;

    public UploadController(JwtService jwtService,
                             AuthorizationService authorizationService,
                             UploadSessionService uploadSessionService,
                             UploadFinalizationService uploadFinalizationService) {
        this.jwtService = jwtService;
        this.authorizationService = authorizationService;
        this.uploadSessionService = uploadSessionService;
        this.uploadFinalizationService = uploadFinalizationService;
    }

    @PostMapping
    public StartUploadSessionResponse startSession(@RequestHeader("Authorization") String authorization,
                                                     @Valid @RequestBody StartUploadSessionRequest request) {
        String userId = jwtService.extractUserId(authorization);

        // TODO: once folder-level entries exist in the permission cache, branch on fileId vs
        // folderId explicitly instead of treating folderId as a fileId lookup.
        String target = request.fileId() != null ? request.fileId() : request.folderId();
        if (!authorizationService.canUpload(userId, target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User " + userId + " lacks EDITOR/OWNER access to " + target);
        }

        return uploadSessionService.startSession(request, userId);
    }

    @GetMapping("/{sessionId}")
    public UploadSessionStatusResponse getStatus(@PathVariable String sessionId) {
        return uploadSessionService.getStatus(sessionId);
    }

    @PostMapping("/{sessionId}/chunks/{chunkIndex}/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void commitChunk(@PathVariable String sessionId,
                             @PathVariable int chunkIndex,
                             @Valid @RequestBody CommitChunkRequest request) {
        uploadSessionService.commitChunk(sessionId, chunkIndex, request.storageKey());
    }

    @PostMapping("/{sessionId}/finalize")
    public ResponseEntity<FinalizeUploadResponse> finalize(@PathVariable String sessionId) {
        return ResponseEntity.ok(uploadFinalizationService.finalizeUpload(sessionId));
    }
}
