package com.example.downloader.api;

import com.example.downloader.service.DownloadLogEntry;
import com.example.downloader.service.DownloadLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DownloadLogRepository downloadLogRepository;

    public AdminController(DownloadLogRepository downloadLogRepository) {
        this.downloadLogRepository = downloadLogRepository;
    }

    @GetMapping("/downloads")
    public List<DownloadLogEntry> downloads(@RequestParam(defaultValue = "200") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 1000);

        return downloadLogRepository.findRecent(safeLimit);
    }
}
