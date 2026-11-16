package com.rockydrive.notification.notify;

import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.common.event.FileUploadedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAffectedUserResolverTest {

    private final DefaultAffectedUserResolver resolver = new DefaultAffectedUserResolver();

    @Test
    void resolvesActorOnlyForFileUploadedEvent() {
        FileUploadedEvent event = new FileUploadedEvent(
                "file-1", "actor-1", 1L, Instant.now(), "report.pdf", 2048L, "application/pdf", 2);

        assertThat(resolver.resolve(event)).containsExactly("actor-1");
    }

    @Test
    void resolvesActorOnlyForFileUpdatedEvent() {
        FileUpdatedEvent event = new FileUpdatedEvent(
                "file-1", "actor-1", 1L, Instant.now(), 10L, "checksum", List.of());

        assertThat(resolver.resolve(event)).containsExactly("actor-1");
    }

    @Test
    void resolvesActorOnlyForFileDeletedEvent() {
        FileDeletedEvent event = new FileDeletedEvent("file-1", "actor-1", 1L, Instant.now(), true);

        assertThat(resolver.resolve(event)).containsExactly("actor-1");
    }

    @Test
    void resolvesActorAndRecipientForFileSharedEvent() {
        FileSharedEvent event = new FileSharedEvent(
                "file-1", "owner-1", 1L, Instant.now(), "recipient-1", "READ", null);

        assertThat(resolver.resolve(event)).containsExactlyInAnyOrder("owner-1", "recipient-1");
    }

    @Test
    void doesNotDuplicateWhenActorSharesWithThemself() {
        FileSharedEvent event = new FileSharedEvent(
                "file-1", "user-1", 1L, Instant.now(), "user-1", "READ", null);

        assertThat(resolver.resolve(event)).containsExactly("user-1");
    }
}
