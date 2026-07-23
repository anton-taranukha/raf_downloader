package com.example.downloader.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;

public record DownloadedFile(
        String fileName,
        String contentType,
        byte[] content,
        List<String> missingFileNames,
        int downloadedFileCount
) {

    public DownloadedFile(String fileName, String contentType, byte[] content) {
        this(fileName, contentType, content, List.of(), 1);
    }

    public Resource resource() {
        return new NamedByteArrayResource(content, fileName);
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String fileName;

        private NamedByteArrayResource(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}
