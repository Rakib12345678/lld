package com.rockydrive.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.common.event.FileUploadedEvent;
import com.rockydrive.notification.fcm.FcmPushService;
import com.rockydrive.notification.model.SyncNotificationPayload;
import com.rockydrive.notification.notify.DefaultAffectedUserResolver;
import com.rockydrive.notification.websocket.WebSocketSessionRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FileEventListenerTest {

    private WebSocketSessionRegistry sessionRegistry;
    private FcmPushService fcmPushService;
    private FileEventListener listener;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sessionRegistry = new WebSocketSessionRegistry();
        fcmPushService = mock(FcmPushService.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new FileEventListener(
                new DefaultAffectedUserResolver(), sessionRegistry, fcmPushService, objectMapper);
    }

    @Test
    void fileUploadedEventFallsBackToFcmForActor() throws Exception {
        FileUploadedEvent event = new FileUploadedEvent(
                "file-4", "user-4", 1L, Instant.now(), "report.pdf", 2048L, "application/pdf", 2);
        ConsumerRecord<String, String> record = consumerRecord("file.uploaded", event);

        listener.onFileEvent(record);

        verify(fcmPushService).sendWakeUpPush(eq("user-4"), any(SyncNotificationPayload.class));
    }

    @Test
    void fileUploadedEventPushesOverWebSocketAndSkipsFcmWhenSessionIsActive() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        sessionRegistry.register("user-4", session);

        FileUploadedEvent event = new FileUploadedEvent(
                "file-4", "user-4", 1L, Instant.now(), "report.pdf", 2048L, "application/pdf", 2);
        ConsumerRecord<String, String> record = consumerRecord("file.uploaded", event);

        listener.onFileEvent(record);

        verify(session).sendMessage(any(TextMessage.class));
        verifyNoInteractions(fcmPushService);
    }

    @Test
    void malformedFileUploadedPayloadDoesNotThrowOrNotifyAnyone() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("file.uploaded", 0, 0L, "file-4", "not valid json");

        listener.onFileEvent(record);

        verify(fcmPushService, never()).sendWakeUpPush(any(), any());
    }

    @Test
    void fallsBackToFcmWhenUserHasNoActiveSession() throws Exception {
        FileUpdatedEvent event = new FileUpdatedEvent(
                "file-1", "user-1", 5L, Instant.now(), 1024L, "checksum", List.of("chunk-1"));
        ConsumerRecord<String, String> record = consumerRecord("file.updated", event);

        listener.onFileEvent(record);

        verify(fcmPushService).sendWakeUpPush(eq("user-1"), any(SyncNotificationPayload.class));
    }

    @Test
    void pushesOverWebSocketAndSkipsFcmWhenSessionIsActive() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        sessionRegistry.register("user-1", session);

        FileUpdatedEvent event = new FileUpdatedEvent(
                "file-1", "user-1", 5L, Instant.now(), 1024L, "checksum", List.of());
        ConsumerRecord<String, String> record = consumerRecord("file.updated", event);

        listener.onFileEvent(record);

        verify(session).sendMessage(any(TextMessage.class));
        verifyNoInteractions(fcmPushService);
    }

    @Test
    void fileDeletedEventFallsBackToFcmForActor() throws Exception {
        FileDeletedEvent event = new FileDeletedEvent("file-2", "user-9", 3L, Instant.now(), false);
        ConsumerRecord<String, String> record = consumerRecord("file.deleted", event);

        listener.onFileEvent(record);

        verify(fcmPushService).sendWakeUpPush(eq("user-9"), any(SyncNotificationPayload.class));
    }

    @Test
    void fileSharedEventNotifiesBothActorAndSharedWithUser() throws Exception {
        FileSharedEvent event = new FileSharedEvent(
                "file-3", "owner-1", 1L, Instant.now(), "recipient-1", "READ", null);
        ConsumerRecord<String, String> record = consumerRecord("file.shared", event);

        listener.onFileEvent(record);

        verify(fcmPushService).sendWakeUpPush(eq("owner-1"), any(SyncNotificationPayload.class));
        verify(fcmPushService).sendWakeUpPush(eq("recipient-1"), any(SyncNotificationPayload.class));
    }

    @Test
    void malformedPayloadDoesNotThrowOrNotifyAnyone() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("file.updated", 0, 0L, "file-1", "not valid json");

        listener.onFileEvent(record);

        verify(fcmPushService, never()).sendWakeUpPush(any(), any());
    }

    private ConsumerRecord<String, String> consumerRecord(String topic, Object event) throws Exception {
        String json = objectMapper.writeValueAsString(event);
        return new ConsumerRecord<>(topic, 0, 0L, "key", json);
    }
}
