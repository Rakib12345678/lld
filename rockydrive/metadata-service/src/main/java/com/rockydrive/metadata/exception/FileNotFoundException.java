package com.rockydrive.metadata.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String fileId) {
        super("File not found: " + fileId);
    }
}
