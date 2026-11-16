package com.rockydrive.common.event;

/**
 * Central registry of Kafka topic names for file-related events.
 *
 * <p>Data-contract rule: these constants are consumed by 2+ services. Values must never be
 * renamed or removed once published; only new constants may be added.
 */
public final class KafkaTopics {

    public static final String FILE_UPLOADED = "file.uploaded";

    public static final String FILE_UPDATED = "file.updated";

    public static final String FILE_DELETED = "file.deleted";

    public static final String FILE_SHARED = "file.shared";

    private KafkaTopics() {
    }
}
