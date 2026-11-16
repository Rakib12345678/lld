package com.rockydrive.sync.service;

import com.rockydrive.sync.dto.ChangeEntryDTO;
import com.rockydrive.sync.dto.DeltaResponse;
import com.rockydrive.sync.model.ChangeLogEntry;
import com.rockydrive.sync.repository.ChangeLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeltaService {

    private final ChangeLogRepository changeLogRepository;

    public DeltaService(ChangeLogRepository changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    public DeltaResponse getDelta(String userId, long cursor) {
        List<ChangeLogEntry> entries =
                changeLogRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(userId, cursor);

        if (entries.isEmpty()) {
            return new DeltaResponse(List.of(), cursor);
        }

        List<ChangeEntryDTO> changes = entries.stream()
                .map(entry -> new ChangeEntryDTO(
                        entry.getFileId(),
                        entry.getChangeType(),
                        entry.getRevision(),
                        entry.getOccurredAt(),
                        entry.getPayload()))
                .toList();

        long nextCursor = entries.get(entries.size() - 1).getRevision();
        return new DeltaResponse(changes, nextCursor);
    }
}
