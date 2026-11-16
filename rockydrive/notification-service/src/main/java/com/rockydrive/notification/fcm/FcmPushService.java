package com.rockydrive.notification.fcm;

import com.rockydrive.notification.model.SyncNotificationPayload;

/**
 * Sends a "wake up and sync" push notification to a user's devices via Firebase Cloud Messaging,
 * used as the fallback when a user has no active WebSocket session on this instance.
 */
public interface FcmPushService {

    /**
     * @param userId  the user to notify
     * @param payload the same minimal sync payload that would have been sent over WebSocket
     */
    void sendWakeUpPush(String userId, SyncNotificationPayload payload);
}
