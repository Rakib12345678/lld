package com.rockydrive.metadata.service;

import com.rockydrive.metadata.entity.FileNode;
import com.rockydrive.metadata.entity.FileType;
import com.rockydrive.metadata.event.FileEventPublisher;
import com.rockydrive.metadata.exception.StaleRevisionException;
import com.rockydrive.metadata.repository.FileNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CommitRevisionConcurrencyTest {

    @Autowired
    private FileTreeService fileTreeService;

    @Autowired
    private FileNodeRepository fileNodeRepository;

    @MockBean
    private FileEventPublisher eventPublisher;

    private String fileId;
    private static final String OWNER = "user-1";

    @BeforeEach
    void createFile() {
        FileNode node = fileTreeService.create(null, "report.pdf", FileType.FILE, OWNER);
        fileId = node.getId();
    }

    @Test
    void commitAgainstCurrentRevisionSucceeds() {
        var result = fileTreeService.commitRevision(fileId, 0, 1024, "checksum-a", List.of("chunk-1"), OWNER);

        assertThat(result.revision()).isEqualTo(1);
        assertThat(fileNodeRepository.findById(fileId).orElseThrow().getRevision()).isEqualTo(1);
    }

    @Test
    void commitAgainstStaleRevisionIsRejectedAndPreservesChangesAsConflictedCopy() {
        fileTreeService.commitRevision(fileId, 0, 1024, "checksum-a", List.of("chunk-1"), OWNER);

        StaleRevisionException ex = assertThrows(StaleRevisionException.class, () ->
                fileTreeService.commitRevision(fileId, 0, 2048, "checksum-b", List.of("chunk-2"), OWNER));

        assertThat(ex.getCurrentRevision()).isEqualTo(1);
        assertThat(ex.getExpectedRevision()).isEqualTo(0);

        FileNode conflictedCopy = fileNodeRepository.findById(ex.getConflictedCopyId()).orElseThrow();
        assertThat(conflictedCopy.getConflictSourceId()).isEqualTo(fileId);
        assertThat(conflictedCopy.getChecksum()).isEqualTo("checksum-b");
        assertThat(conflictedCopy.getName()).contains("conflicted copy").contains("report").endsWith(".pdf");

        assertThat(fileNodeRepository.findById(fileId).orElseThrow().getChecksum()).isEqualTo("checksum-a");
    }

    @Test
    void concurrentCommitsAgainstSameRevisionOnlyOneWins() throws InterruptedException {
        int attempts = 5;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger conflicted = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            int attempt = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    fileTreeService.commitRevision(
                            fileId, 0, 100 + attempt, "checksum-" + attempt, List.of("chunk-" + attempt), OWNER);
                    succeeded.incrementAndGet();
                } catch (StaleRevisionException e) {
                    conflicted.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        go.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(succeeded.get()).isEqualTo(1);
        assertThat(conflicted.get()).isEqualTo(attempts - 1);
        assertThat(fileNodeRepository.findById(fileId).orElseThrow().getRevision()).isEqualTo(1);
    }
}
