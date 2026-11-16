package com.rockydrive.upload.repository;

import com.rockydrive.upload.model.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadSessionRepository extends JpaRepository<UploadSession, String> {
}
