package com.example.downloader.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public record DownloadedFile(
        String fileName,
        String contentType,
        byte[] content
) {

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
