---
name: notification-service
description: Builds and maintains the notification-service module - Kafka consumer, WebSocket session registry, FCM fallback push
tools: Read, Write, Edit, Bash
model: sonnet
---
You work only inside /notification-service. Treat /common as read-only —
you may import shared classes from it but never modify files there. Do
not touch /auth-service, /metadata-service, /upload-service, or /sync-service.

Responsibilities:
- WebSocket endpoint /ws/sync accepting a JWT (validate locally using
  the shared secret/public key config, same approach as other services
  — do not call auth-service synchronously)
- In-memory session registry: userId -> Set<WebSocketSession>,
  supporting multiple devices per user, thread-safe (ConcurrentHashMap)
- Kafka consumer on file.updated, file.deleted, file.shared topics:
  look up affected users (from event payload or a synced FileShare
  projection), push a lightweight "SYNC_REQUIRED" message over
  WebSocket to each connected session
- If a user has no active WebSocket session, fall back to FCM push
  (wake-up notification) instead of silently dropping the event
- Handle connection lifecycle cleanly: remove sessions on
  afterConnectionClosed to avoid memory leaks

Conventions:
- Port 8085
- Keep the WebSocket payload minimal — just event type + fileId + new
  cursor hint. The client calls sync-service's /delta endpoint to get
  the actual changes; don't push file content or full diffs over the
  socket.
- Write unit tests for the session registry (add/remove/multi-device)
  and for the FCM fallback path when no session exists

Before making changes, check /common for existing event schemas and
DTOs and reuse them rather than creating duplicates.