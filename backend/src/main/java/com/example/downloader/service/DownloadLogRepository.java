package com.example.downloader.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class DownloadLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public DownloadLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordSuccess(String fileName) {
        record(fileName, "success", null);
    }

    public void recordError(String fileName, String message) {
        record(fileName, "error", message);
    }

    public List<DownloadLogEntry> findRecent(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, file_name, status, message, requested_at
                FROM download_log
                ORDER BY requested_at DESC, id DESC
                LIMIT ?
                """,
                this::mapEntry,
                limit
        );
    }

    private void record(String fileName, String status, String message) {
        jdbcTemplate.update(
                "INSERT INTO download_log (file_name, status, message) VALUES (?, ?, ?)",
                fileName,
                status,
                message
        );
    }

    private DownloadLogEntry mapEntry(ResultSet resultSet, int rowNumber) throws SQLException {
        return new DownloadLogEntry(
                resultSet.getLong("id"),
                resultSet.getString("file_name"),
                resultSet.getString("status"),
                resultSet.getString("message"),
                resultSet.getObject("requested_at", OffsetDateTime.class)
        );
    }
}
