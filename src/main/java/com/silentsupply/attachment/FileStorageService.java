package com.silentsupply.attachment;

import com.silentsupply.common.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for storing, loading, and deleting files on the local filesystem.
 * Files are stored with UUID-based paths to avoid name collisions.
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path storageRoot;

    /**
     * Creates a FileStorageService with the configured storage path.
     *
     * @param storagePath the root directory for file storage
     */
    public FileStorageService(@Value("${app.attachments.storage-path:./uploads}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    /**
     * Initializes the storage directory on startup.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageRoot);
            log.info("File storage initialized at: {}", storageRoot);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directory: " + storageRoot, e);
        }
    }

    /**
     * Stores a file and returns the relative storage path.
     *
     * @param file the multipart file to store
     * @return the relative path where the file was stored
     */
    public String store(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path targetPath = storageRoot.resolve(filename).normalize();

        if (!targetPath.startsWith(storageRoot)) {
            throw new FileStorageException("Cannot store file outside storage directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file: {}", filename);
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    /**
     * Loads a file as a Spring Resource.
     *
     * @param storagePath the relative storage path
     * @return the file as a Resource
     */
    public Resource load(String storagePath) {
        try {
            Path filePath = storageRoot.resolve(storagePath).normalize();
            if (!filePath.startsWith(storageRoot)) {
                throw new FileStorageException("Cannot access file outside storage directory");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found or not readable: " + storagePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("Invalid file path: " + storagePath, e);
        }
    }

    /**
     * Deletes a file from storage.
     *
     * @param storagePath the relative storage path
     */
    public void delete(String storagePath) {
        try {
            Path filePath = storageRoot.resolve(storagePath).normalize();
            if (!filePath.startsWith(storageRoot)) {
                throw new FileStorageException("Cannot delete file outside storage directory");
            }
            Files.deleteIfExists(filePath);
            log.debug("Deleted file: {}", storagePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + storagePath, e);
        }
    }

    /**
     * Sanitizes a filename by removing path separators.
     *
     * @param filename the original filename
     * @return the sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        return filename.replaceAll("[/\\\\]", "_");
    }
}
