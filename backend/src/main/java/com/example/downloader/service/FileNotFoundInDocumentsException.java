package com.example.downloader.service;

public class FileNotFoundInDocumentsException extends RuntimeException {

    public FileNotFoundInDocumentsException(String message) {
        super(message);
    }
}
