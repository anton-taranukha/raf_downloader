package com.example.downloader.service;

import com.example.downloader.config.DocumentsProperties;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileDownloadService {

    private static final String GOOGLE_DOCS_MIME_TYPE = "application/vnd.google-apps.document";
    private static final String GOOGLE_SHEETS_MIME_TYPE = "application/vnd.google-apps.spreadsheet";
    private static final String GOOGLE_SLIDES_MIME_TYPE = "application/vnd.google-apps.presentation";
    private static final Map<String, ExportFormat> EXPORT_FORMATS = Map.of(
            GOOGLE_DOCS_MIME_TYPE, new ExportFormat("application/pdf", ".pdf"),
            GOOGLE_SHEETS_MIME_TYPE, new ExportFormat("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
            GOOGLE_SLIDES_MIME_TYPE, new ExportFormat("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx")
    );

    private final Drive drive;
    private final String folderId;

    public FileDownloadService(Drive drive, DocumentsProperties documentsProperties) {
        this.drive = drive;
        this.folderId = documentsProperties.folderId();
    }

    public DownloadedFile load(String fileName) {
        String normalizedFileName = fileName.trim();
        if (normalizedFileName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        try {
            File file = findFile(normalizedFileName);
            ExportFormat exportFormat = EXPORT_FORMATS.get(file.getMimeType());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (exportFormat == null) {
                drive.files()
                        .get(file.getId())
                        .setSupportsAllDrives(true)
                        .executeMediaAndDownloadTo(outputStream);

                return new DownloadedFile(file.getName(), file.getMimeType(), outputStream.toByteArray());
            }

            drive.files()
                    .export(file.getId(), exportFormat.mimeType())
                    .executeMediaAndDownloadTo(outputStream);

            return new DownloadedFile(file.getName() + exportFormat.extension(), exportFormat.mimeType(), outputStream.toByteArray());
        } catch (IOException exception) {
            throw new GoogleDriveAccessException("Could not download file from Google Drive", exception);
        }
    }

    public DownloadedFile loadArchive(List<String> fileNames) {
        if (fileNames == null) {
            throw new IllegalArgumentException("At least one file name is required");
        }

        List<String> normalizedFileNames = fileNames.stream()
                .map(String::trim)
                .filter(fileName -> !fileName.isEmpty())
                .toList();

        if (normalizedFileNames.isEmpty()) {
            throw new IllegalArgumentException("At least one file name is required");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                Map<String, Integer> usedEntryNames = new HashMap<>();

                for (String fileName : normalizedFileNames) {
                    DownloadedFile downloadedFile = load(fileName);
                    String entryName = uniqueEntryName(downloadedFile.fileName(), usedEntryNames);
                    ZipEntry entry = new ZipEntry(entryName);
                    zipOutputStream.putNextEntry(entry);
                    zipOutputStream.write(downloadedFile.content());
                    zipOutputStream.closeEntry();
                }
            }

            return new DownloadedFile("dorozhni.zip", "application/zip", outputStream.toByteArray());
        } catch (IOException exception) {
            throw new GoogleDriveAccessException("Could not create download archive", exception);
        }
    }

    private File findFile(String fileName) throws IOException {
        String query = "'%s' in parents and name = '%s' and trashed = false"
                .formatted(escapeQueryValue(folderId), escapeQueryValue(fileName));

        var files = drive.files()
                .list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id,name,mimeType,modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .setPageSize(1)
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute()
                .getFiles();

        if (files == null || files.isEmpty()) {
            throw new FileNotFoundInDocumentsException("File was not found");
        }

        return files.getFirst();
    }

    private String escapeQueryValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private String uniqueEntryName(String fileName, Map<String, Integer> usedEntryNames) {
        String sanitizedName = fileName.replace("\\", "_").replace("/", "_");
        int count = usedEntryNames.getOrDefault(sanitizedName, 0);
        usedEntryNames.put(sanitizedName, count + 1);

        if (count == 0) {
            return sanitizedName;
        }

        int extensionIndex = sanitizedName.lastIndexOf('.');
        if (extensionIndex <= 0) {
            return sanitizedName + " (" + count + ")";
        }

        return sanitizedName.substring(0, extensionIndex)
                + " (" + count + ")"
                + sanitizedName.substring(extensionIndex);
    }

    private record ExportFormat(String mimeType, String extension) {
    }
}
