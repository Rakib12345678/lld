package com.rockydrive.metadata.exception;

/**
 * Thrown when a commit targets a revision older than the server's current one. The caller's
 * changes are not lost - the service persists them as a conflicted copy and reports its id here.
 */
public class StaleRevisionException extends RuntimeException {

    private final String fileId;
    private final long currentRevision;
    private final long expectedRevision;
    private final String conflictedCopyId;

    public StaleRevisionException(String fileId, long currentRevision, long expectedRevision, String conflictedCopyId) {
        super("Commit rejected for file %s: current revision is %d but commit expected %d"
                .formatted(fileId, currentRevision, expectedRevision));
        this.fileId = fileId;
        this.currentRevision = currentRevision;
        this.expectedRevision = expectedRevision;
        this.conflictedCopyId = conflictedCopyId;
    }

    public String getFileId() {
        return fileId;
    }

    public long getCurrentRevision() {
        return currentRevision;
    }

    public long getExpectedRevision() {
        return expectedRevision;
    }

    public String getConflictedCopyId() {
        return conflictedCopyId;
    }
}
