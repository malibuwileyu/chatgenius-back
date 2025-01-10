package com.chatgenius.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * Uploads a file to the storage service.
     * @param file The file to upload
     * @return The URL of the uploaded file
     */
    String uploadFile(MultipartFile file);

    /**
     * Deletes a file from the storage service.
     * @param fileUrl The URL of the file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Gets a download URL for a file.
     * @param fileUrl The URL of the file
     * @return The download URL
     */
    String getDownloadUrl(String fileUrl);
} 