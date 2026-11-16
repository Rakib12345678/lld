package com.rockydrive.notification.notify;

import com.rockydrive.common.event.FileEvent;
import com.rockydrive.common.event.FileSharedEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves affected users directly from the event payload, without any synchronous lookup to
 * another service.
 *
 * <p>Every {@link FileEvent} carries {@code actorId}, so the actor is always notified (so their
 * other devices stay in sync). {@link FileSharedEvent} additionally names the user the file was
 * shared with, who is notified as well.
 *
 * <p>Note: {@code file.uploaded}/{@code file.updated}/{@code file.deleted} events do not carry a
 * full collaborator list on the wire (only the acting user), so this resolver cannot fan a change out to every
 * collaborator on a shared file purely from the event payload - doing that would require a
 * synchronous lookup against metadata-service's share/collaborator projection, which is out of
 * scope for this service (notification-service intentionally does not call other services
 * synchronously). If/when a materialized "file collaborators" projection is available locally
 * (e.g. via its own Kafka topic), this is the seam to extend.
 */
@Component
public class DefaultAffectedUserResolver implements AffectedUserResolver {

    @Override
    public Set<String> resolve(FileEvent event) {
        Set<String> userIds = new LinkedHashSet<>();
        if (event.actorId() != null && !event.actorId().isBlank()) {
            userIds.add(event.actorId());
        }
        if (event instanceof FileSharedEvent sharedEvent
                && sharedEvent.sharedWithUserId() != null
                && !sharedEvent.sharedWithUserId().isBlank()) {
            userIds.add(sharedEvent.sharedWithUserId());
        }
        return userIds;
    }
}
