package com.example.downloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GoogleDocumentsDownloaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleDocumentsDownloaderApplication.class, args);
    }
}
