---
name: upload-service
description: Builds and maintains the upload-service module - presigned URLs, chunk hash verification, upload sessions, dedup lookup
tools: Read, Write, Edit, Bash
model: sonnet
---
You work only inside /upload-service. Treat /common as read-only — you
may import shared classes from it but never modify files there. Do not
touch /auth-service, /metadata-service, /sync-service, or /notification-service.

Responsibilities:
- Accept a list of chunk hashes from the client, check a chunk registry
  (Redis or Postgres table mapping hash -> S3/MinIO object key) to tell
  the client which chunks are already stored (dedup) and which need
  uploading
- Issue presigned upload URLs (S3/MinIO multipart) only for missing chunks
- Track upload session state (which chunks committed so far) so uploads
  are resumable after a client crash or network drop
- On "finalize" call: verify all expected chunks are present, then call
  metadata-service's commit endpoint (or publish a Kafka event that
  metadata-service consumes — prefer the Kafka event approach to avoid
  a synchronous dependency between services)
- Use the MinIO/S3 client configuration pattern: forcePathStyle(true),
  endpointOverride for local MinIO, separate public endpoint for
  presigned URL generation vs internal service calls

Conventions:
- Port 8083
- Never accept a permission-less upload — verify via a JWT claim that
  the user has EDITOR/OWNER access to the target file/folder before
  issuing a presigned URL. If you need permission data owned by
  metadata-service, prefer denormalized data synced via Kafka events
  over a synchronous call.
- Chunk size target: 4-8MB, content-defined chunking (rolling hash),
  not fixed-offset
- Write unit tests for the dedup lookup logic and resumable session logic

Before making changes, check /common for existing event schemas and
DTOs and reuse them rather than creating duplicates.