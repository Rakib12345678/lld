package com.rockydrive.notification.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.rockydrive.notification.model.SyncNotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Live FCM-backed implementation of {@link FcmPushService}. Only registered when
 * {@code notification.fcm.enabled=true} (see {@link com.rockydrive.notification.config.FirebaseConfig}).
 *
 * <p>Rather than requiring a per-device token registry, each user's devices are expected to
 * subscribe (client-side, after login) to their own FCM topic named {@code user-<userId>}; this
 * service simply publishes a small data-only message to that topic. Data-only (no {@code
 * notification} block) so the payload stays the same minimal "sync required" shape used over
 * WebSocket, and the client app decides how/whether to surface a visible notification.
 */
@Service
@ConditionalOnProperty(prefix = "notification.fcm", name = "enabled", havingValue = "true")
public class FirebaseFcmPushService implements FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseFcmPushService.class);
    private static final String USER_TOPIC_PREFIX = "user-";

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseFcmPushService(FirebaseApp firebaseApp) {
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    @Override
    public void sendWakeUpPush(String userId, SyncNotificationPayload payload) {
        Message message = Message.builder()
                .setTopic(USER_TOPIC_PREFIX + userId)
                .putData("kind", payload.kind())
                .putData("eventType", payload.eventType())
                .putData("fileId", payload.fileId())
                .putData("revision", String.valueOf(payload.revision()))
                .build();
        try {
            String messageId = firebaseMessaging.send(message);
            log.debug("Sent FCM wake-up push {} to user {}", messageId, userId);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to send FCM wake-up push to user {}: {}", userId, e.getMessage());
        }
    }
}
