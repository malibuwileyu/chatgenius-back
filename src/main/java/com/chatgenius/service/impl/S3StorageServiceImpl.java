package com.chatgenius.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.chatgenius.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.url-prefix}")
    private String urlPrefix;

    @Override
    public String uploadFile(MultipartFile file) {
        logger.debug("Uploading file: {}", file.getOriginalFilename());
        
        try {
            String key = generateKey(file.getOriginalFilename());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            s3Client.putObject(request);

            String fileUrl = urlPrefix + "/" + key;
            logger.info("Successfully uploaded file to: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        logger.debug("Deleting file: {}", fileUrl);
        
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(bucketName, key);
            logger.info("Successfully deleted file: {}", fileUrl);
        } catch (Exception e) {
            logger.error("Failed to delete file", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String getDownloadUrl(String fileUrl) {
        logger.debug("Getting download URL for: {}", fileUrl);
        
        try {
            String key = extractKeyFromUrl(fileUrl);
            return s3Client.getUrl(bucketName, key).toString();
        } catch (Exception e) {
            logger.error("Failed to get download URL", e);
            throw new RuntimeException("Failed to get download URL", e);
        }
    }

    private String generateKey(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return UUID.randomUUID().toString() + extension;
    }

    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.substring(urlPrefix.length() + 1);
    }
} 