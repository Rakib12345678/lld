package com.rockydrive.sync.repository;

import com.rockydrive.sync.model.ChangeLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeLogRepository extends JpaRepository<ChangeLogEntry, Long> {

    List<ChangeLogEntry> findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(String userId, long cursor);
}
