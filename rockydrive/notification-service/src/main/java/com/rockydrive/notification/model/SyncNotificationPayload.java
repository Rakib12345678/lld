package com.rockydrive.notification.model;

/**
 * Minimal "you need to sync" signal pushed over WebSocket (and, as a fallback, via FCM data
 * payload). Deliberately does not carry file content or diffs - the client is expected to call
 * sync-service's delta endpoint, keyed by {@code fileId}/{@code revision}, to fetch the actual
 * changes.
 *
 * @param kind      always {@code "SYNC_REQUIRED"} - lets clients distinguish this from any future
 *                  message kind on the same socket
 * @param eventType the file event that triggered this push, e.g. "FILE_UPDATED", "FILE_DELETED",
 *                  "FILE_SHARED"
 * @param fileId    id of the file that changed
 * @param revision  revision/cursor hint the client can use to decide whether it is already caught up
 */
public record SyncNotificationPayload(String kind, String eventType, String fileId, long revision) {

    public static final String KIND_SYNC_REQUIRED = "SYNC_REQUIRED";

    public SyncNotificationPayload(String eventType, String fileId, long revision) {
        this(KIND_SYNC_REQUIRED, eventType, fileId, revision);
    }
}
