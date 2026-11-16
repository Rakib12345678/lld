package com.rockydrive.sync.service;

import com.rockydrive.sync.dto.DeltaResponse;
import com.rockydrive.sync.model.ChangeLogEntry;
import com.rockydrive.sync.model.ChangeType;
import com.rockydrive.sync.repository.ChangeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeltaServiceTest {

    @Mock
    private ChangeLogRepository changeLogRepository;

    @Test
    void noChangesSinceCursor_returnsEmptyChangesAndSameCursor() {
        DeltaService service = new DeltaService(changeLogRepository);
        when(changeLogRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc("user-1", 42L))
                .thenReturn(List.of());

        DeltaResponse response = service.getDelta("user-1", 42L);

        assertThat(response.changes()).isEmpty();
        assertThat(response.nextCursor()).isEqualTo(42L);
    }

    @Test
    void cursorFarInThePast_returnsFullDiffAndAdvancesCursorToLatestRevision() {
        DeltaService service = new DeltaService(changeLogRepository);
        List<ChangeLogEntry> entries = List.of(
                new ChangeLogEntry("user-1", "file-1", ChangeType.ADDED, 1L, Instant.parse("2026-01-01T00:00:00Z"), "{}"),
                new ChangeLogEntry("user-1", "file-2", ChangeType.UPDATED, 2L, Instant.parse("2026-01-02T00:00:00Z"), "{}"),
                new ChangeLogEntry("user-1", "file-1", ChangeType.DELETED, 3L, Instant.parse("2026-01-03T00:00:00Z"), "{}")
        );
        when(changeLogRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc("user-1", 0L))
                .thenReturn(entries);

        DeltaResponse response = service.getDelta("user-1", 0L);

        assertThat(response.changes()).hasSize(3);
        assertThat(response.nextCursor()).isEqualTo(3L);
    }
}
