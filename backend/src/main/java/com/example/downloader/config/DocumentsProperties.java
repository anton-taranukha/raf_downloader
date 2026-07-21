package com.example.downloader.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google-drive")
public record DocumentsProperties(
        @NotBlank String folderId,
        String credentialsPath
) {
}
