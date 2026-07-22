package com.example.downloader.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ComposeConfigurationTest {

    @Test
    void composeFilesUseSharedGoogleDriveEnvironmentFile() throws IOException {
        Path root = projectRoot();

        String localCompose = Files.readString(root.resolve("docker-compose.yml"));
        String serverCompose = Files.readString(root.resolve("docker-compose.server.yml"));
        String driveEnvironment = Files.readString(root.resolve("google-drive.env"));

        assertThat(driveEnvironment)
                .contains("GOOGLE_DRIVE_FOLDER_ID=1pjR3vYJMn814Fy0XJDOUGkSBvGYeG0o-");

        assertThat(localCompose)
                .contains("./google-drive.env")
                .doesNotContain("GOOGLE_DRIVE_FOLDER_ID:");
        assertThat(serverCompose)
                .contains("./google-drive.env")
                .doesNotContain("GOOGLE_DRIVE_FOLDER_ID:");
    }

    private Path projectRoot() {
        Path currentDirectory = Path.of("").toAbsolutePath().normalize();

        if (Files.exists(currentDirectory.resolve("docker-compose.yml"))) {
            return currentDirectory;
        }

        return currentDirectory.getParent();
    }
}
