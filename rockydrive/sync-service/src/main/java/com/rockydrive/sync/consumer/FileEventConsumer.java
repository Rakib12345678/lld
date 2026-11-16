package com.rockydrive.sync.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.common.event.FileUploadedEvent;
import com.rockydrive.common.event.KafkaTopics;
import com.rockydrive.sync.model.ChangeLogEntry;
import com.rockydrive.sync.model.ChangeType;
import com.rockydrive.sync.repository.ChangeLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Projects file.* events from metadata-service into this service's own changelog table
 * (never reads metadata-service's tables directly, so the two services stay decoupled).
 */
@Component
public class FileEventConsumer {

    private final ChangeLogRepository changeLogRepository;
    private final ObjectMapper objectMapper;

    public FileEventConsumer(ChangeLogRepository changeLogRepository, ObjectMapper objectMapper) {
        this.changeLogRepository = changeLogRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.FILE_UPLOADED, groupId = "sync-service")
    public void onFileUploaded(String message) throws Exception {
        FileUploadedEvent event = objectMapper.readValue(message, FileUploadedEvent.class);
        save(event.actorId(), event.fileId(), ChangeType.ADDED, event.revision(), event.timestamp(), message);
    }

    @KafkaListener(topics = KafkaTopics.FILE_UPDATED, groupId = "sync-service")
    public void onFileUpdated(String message) throws Exception {
        FileUpdatedEvent event = objectMapper.readValue(message, FileUpdatedEvent.class);
        save(event.actorId(), event.fileId(), ChangeType.UPDATED, event.revision(), event.timestamp(), message);
    }

    @KafkaListener(topics = KafkaTopics.FILE_DELETED, groupId = "sync-service")
    public void onFileDeleted(String message) throws Exception {
        FileDeletedEvent event = objectMapper.readValue(message, FileDeletedEvent.class);
        save(event.actorId(), event.fileId(), ChangeType.DELETED, event.revision(), event.timestamp(), message);
    }

    @KafkaListener(topics = KafkaTopics.FILE_SHARED, groupId = "sync-service")
    public void onFileShared(String message) throws Exception {
        FileSharedEvent event = objectMapper.readValue(message, FileSharedEvent.class);
        // The recipient is the one who needs this file to show up in their next delta, not the
        // sharer - so the changelog row is keyed on sharedWithUserId rather than actorId.
        save(event.sharedWithUserId(), event.fileId(), ChangeType.SHARED, event.revision(), event.timestamp(), message);
    }

    private void save(String userId, String fileId, ChangeType changeType, long revision,
                       java.time.Instant timestamp, String payload) {
        changeLogRepository.save(new ChangeLogEntry(userId, fileId, changeType, revision, timestamp, payload));
    }
}
