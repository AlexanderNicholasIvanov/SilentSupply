package com.silentsupply.attachment;

import com.silentsupply.common.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FileStorageService}.
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    void store_validFile_returnsStoragePath() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        String storagePath = fileStorageService.store(file);

        assertThat(storagePath).isNotBlank();
        assertThat(storagePath).endsWith("_test.pdf");
    }

    @Test
    void load_existingFile_returnsResource() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes());
        String storagePath = fileStorageService.store(file);

        Resource resource = fileStorageService.load(storagePath);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void load_nonExistentFile_throwsException() {
        assertThatThrownBy(() -> fileStorageService.load("nonexistent.txt"))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void delete_existingFile_removesFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "to-delete.txt", "text/plain", "delete me".getBytes());
        String storagePath = fileStorageService.store(file);

        fileStorageService.delete(storagePath);

        assertThatThrownBy(() -> fileStorageService.load(storagePath))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void store_nullFilename_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "application/octet-stream", "data".getBytes());

        String storagePath = fileStorageService.store(file);

        assertThat(storagePath).isNotBlank();
    }
}
