package com.example.downloader.api;

import com.example.downloader.service.DownloadedFile;
import com.example.downloader.service.FileDownloadService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    public FileDownloadController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam @NotBlank String fileName) {
        DownloadedFile downloadedFile = fileDownloadService.load(fileName);

        return fileResponse(downloadedFile);
    }

    @PostMapping("/download-batch")
    public ResponseEntity<Resource> downloadBatch(@RequestBody BatchDownloadRequest request) {
        DownloadedFile downloadedFile = fileDownloadService.loadArchive(request.fileNames());

        return fileResponse(downloadedFile);
    }

    private ResponseEntity<Resource> fileResponse(DownloadedFile downloadedFile) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadedFile.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header("X-Downloaded-File-Count", Integer.toString(downloadedFile.downloadedFileCount()))
                .header("X-Missing-Files", encodeMissingFileNames(downloadedFile.missingFileNames()))
                .body(downloadedFile.resource());
    }

    private String encodeMissingFileNames(List<String> missingFileNames) {
        return URLEncoder.encode(String.join("\n", missingFileNames), StandardCharsets.UTF_8);
    }

    public record BatchDownloadRequest(List<String> fileNames) {
    }
}
