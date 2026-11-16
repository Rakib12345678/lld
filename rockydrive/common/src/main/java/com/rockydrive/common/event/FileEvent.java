package com.rockydrive.common.event;

import java.time.Instant;

/**
 * Common shape shared by every file-related Kafka event in rockydrive.
 *
 * <p>This is a plain marker interface (no default methods, no logic) so that consumers who only
 * care about the common envelope fields can code against it generically, e.g. {@code
 * List<FileEvent>} for audit logging. It does not participate in Jackson polymorphic
 * deserialization - each event type is still published/consumed as its own concrete record on its
 * own topic (or discriminated by a message key/header), keeping wire formats simple.
 *
 * <p>Data-contract rule: every implementor must expose these four fields. New event types may add
 * their own event-specific fields, but must never remove or repurpose these.
 */
public interface FileEvent {

    String fileId();

    String actorId();

    long revision();

    Instant timestamp();
}
