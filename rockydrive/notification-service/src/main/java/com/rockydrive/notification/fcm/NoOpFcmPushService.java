package com.rockydrive.notification.fcm;

import com.rockydrive.notification.model.SyncNotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default, no-live-credentials-required stand-in for {@link FcmPushService}. Active whenever
 * {@code notification.fcm.enabled} is not {@code true} (including when it is unset), which is
 * the default - this lets the module build, run, and be tested without any Firebase project
 * configured. Simply logs what would have been pushed.
 */
@Service
@ConditionalOnProperty(prefix = "notification.fcm", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFcmPushService implements FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(NoOpFcmPushService.class);

    @Override
    public void sendWakeUpPush(String userId, SyncNotificationPayload payload) {
        log.info("[FCM no-op] would push wake-up notification to user {}: {}", userId, payload);
    }
}
