package com.example.downloader.service;

import java.time.OffsetDateTime;

public record DownloadLogEntry(
        long id,
        String fileName,
        String status,
        String message,
        OffsetDateTime requestedAt
) {
}
