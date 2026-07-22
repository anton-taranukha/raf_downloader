package com.example.downloader.api;

import com.example.downloader.service.FileNotFoundInDocumentsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    @Test
    void fileNotFoundResponseDoesNotExposeStorageDetails() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ProblemDetail problem = handler.handleFileNotFound(
                new FileNotFoundInDocumentsException("File was not found")
        );

        assertThat(problem.getDetail()).isEqualTo("File was not found");
        assertThat(problem.getDetail()).doesNotContain("Google Drive");
        assertThat(problem.getDetail()).doesNotContain("folder");
    }
}
