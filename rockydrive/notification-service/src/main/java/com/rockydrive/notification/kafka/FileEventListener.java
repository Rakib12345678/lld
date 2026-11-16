package com.rockydrive.notification.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileEvent;
import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.common.event.FileUploadedEvent;
import com.rockydrive.common.event.KafkaTopics;
import com.rockydrive.notification.fcm.FcmPushService;
import com.rockydrive.notification.model.SyncNotificationPayload;
import com.rockydrive.notification.notify.AffectedUserResolver;
import com.rockydrive.notification.websocket.WebSocketSessionRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

/**
 * Consumes the file.uploaded / file.updated / file.deleted / file.shared topics and turns each
 * event into a minimal "SYNC_REQUIRED" push to every affected user: over an open WebSocket
 * session if one exists, otherwise via an FCM wake-up push so the event is never silently
 * dropped.
 *
 * <p>Events are consumed as raw JSON strings (rather than relying on a producer-set Jackson type
 * header) and deserialized per-topic into the matching {@link FileEvent} record from the shared
 * {@code common} module, so this listener doesn't depend on how the (separately-owned) producer
 * services configure their Kafka value serializer.
 */
@Component
public class FileEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileEventListener.class);

    static final String EVENT_TYPE_FILE_UPLOADED = "FILE_UPLOADED";
    static final String EVENT_TYPE_FILE_UPDATED = "FILE_UPDATED";
    static final String EVENT_TYPE_FILE_DELETED = "FILE_DELETED";
    static final String EVENT_TYPE_FILE_SHARED = "FILE_SHARED";

    private final AffectedUserResolver affectedUserResolver;
    private final WebSocketSessionRegistry sessionRegistry;
    private final FcmPushService fcmPushService;
    private final ObjectMapper objectMapper;

    public FileEventListener(AffectedUserResolver affectedUserResolver,
                              WebSocketSessionRegistry sessionRegistry,
                              FcmPushService fcmPushService,
                              ObjectMapper objectMapper) {
        this.affectedUserResolver = affectedUserResolver;
        this.sessionRegistry = sessionRegistry;
        this.fcmPushService = fcmPushService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {KafkaTopics.FILE_UPLOADED, KafkaTopics.FILE_UPDATED, KafkaTopics.FILE_DELETED, KafkaTopics.FILE_SHARED},
            groupId = "${notification.kafka.consumer-group:notification-service}"
    )
    public void onFileEvent(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        try {
            switch (topic) {
                case KafkaTopics.FILE_UPLOADED -> handle(
                        EVENT_TYPE_FILE_UPLOADED,
                        objectMapper.readValue(record.value(), FileUploadedEvent.class));
                case KafkaTopics.FILE_UPDATED -> handle(
                        EVENT_TYPE_FILE_UPDATED,
                        objectMapper.readValue(record.value(), FileUpdatedEvent.class));
                case KafkaTopics.FILE_DELETED -> handle(
                        EVENT_TYPE_FILE_DELETED,
                        objectMapper.readValue(record.value(), FileDeletedEvent.class));
                case KafkaTopics.FILE_SHARED -> handle(
                        EVENT_TYPE_FILE_SHARED,
                        objectMapper.readValue(record.value(), FileSharedEvent.class));
                default -> log.warn("Received event from unexpected topic {}, ignoring", topic);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event from topic {} (offset {}): {}",
                    topic, record.offset(), e.getMessage());
        }
    }

    private void handle(String eventType, FileEvent event) {
        SyncNotificationPayload payload =
                new SyncNotificationPayload(eventType, event.fileId(), event.revision());

        Set<String> affectedUserIds = affectedUserResolver.resolve(event);
        if (affectedUserIds.isEmpty()) {
            log.debug("No affected users resolved for {} event on file {}", eventType, event.fileId());
            return;
        }

        for (String userId : affectedUserIds) {
            if (sessionRegistry.hasActiveSession(userId)) {
                pushOverWebSocket(userId, payload);
            } else {
                fcmPushService.sendWakeUpPush(userId, payload);
            }
        }
    }

    private void pushOverWebSocket(String userId, SyncNotificationPayload payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize sync payload for user {}: {}", userId, e.getMessage());
            return;
        }

        TextMessage message = new TextMessage(json);
        for (WebSocketSession session : sessionRegistry.getSessions(userId)) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.warn("Failed to push sync message to session {} for user {}: {}",
                        session.getId(), userId, e.getMessage());
            }
        }
    }
}
