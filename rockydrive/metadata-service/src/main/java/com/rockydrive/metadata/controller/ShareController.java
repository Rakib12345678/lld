package com.rockydrive.metadata.controller;

import com.rockydrive.metadata.dto.FileShareResponse;
import com.rockydrive.metadata.dto.ShareRequest;
import com.rockydrive.metadata.security.JwtAuthFilter;
import com.rockydrive.metadata.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files/{fileId}/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<FileShareResponse> share(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId,
            @Valid @RequestBody ShareRequest request) {
        var share = shareService.share(fileId, request.sharedWithUserId(), request.permission(), actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileShareResponse.from(share));
    }

    @GetMapping
    public List<FileShareResponse> list(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String actorId,
            @PathVariable String fileId) {
        return shareService.listShares(fileId, actorId).stream()
                .map(FileShareResponse::from)
                .toList();
    }
}
