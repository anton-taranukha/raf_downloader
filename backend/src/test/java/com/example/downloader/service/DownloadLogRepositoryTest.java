package com.example.downloader.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadLogRepositoryTest {

    @Test
    void recordsAndReturnsRecentDownloadAttempts() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:download-log-test;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
        }

        DownloadLogRepository repository = new DownloadLogRepository(new JdbcTemplate(dataSource));

        repository.recordSuccess("2394д.pdf");
        repository.recordError("11111.pdf", "File was not found");

        List<DownloadLogEntry> entries = repository.findRecent(10);

        assertThat(entries).hasSize(2);
        assertThat(entries.getFirst().fileName()).isEqualTo("11111.pdf");
        assertThat(entries.getFirst().status()).isEqualTo("error");
        assertThat(entries.getFirst().message()).isEqualTo("File was not found");
        assertThat(entries.getFirst().requestedAt()).isNotNull();
        assertThat(entries.get(1).fileName()).isEqualTo("2394д.pdf");
        assertThat(entries.get(1).status()).isEqualTo("success");
    }
}
