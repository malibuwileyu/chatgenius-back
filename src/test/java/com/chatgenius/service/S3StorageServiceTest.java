package com.chatgenius.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.chatgenius.service.impl.S3StorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private AmazonS3 s3Client;

    private S3StorageServiceImpl storageService;
    private static final String BUCKET_NAME = "test-bucket";
    private static final String URL_PREFIX = "https://test-bucket.s3.amazonaws.com";

    @BeforeEach
    void setUp() {
        storageService = new S3StorageServiceImpl(s3Client);
        ReflectionTestUtils.setField(storageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(storageService, "urlPrefix", URL_PREFIX);
    }

    @Test
    void uploadFile_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "Hello, World!".getBytes()
        );

        // Act
        String result = storageService.uploadFile(file);

        // Assert
        verify(s3Client).putObject(any(PutObjectRequest.class));
        assertTrue(result.startsWith(URL_PREFIX));
        assertTrue(result.endsWith(".txt"));
    }

    @Test
    void deleteFile_Success() {
        // Arrange
        String fileUrl = URL_PREFIX + "/test.txt";

        // Act
        storageService.deleteFile(fileUrl);

        // Assert
        verify(s3Client).deleteObject(BUCKET_NAME, "test.txt");
    }

    @Test
    void getDownloadUrl_Success() throws Exception {
        // Arrange
        String fileUrl = URL_PREFIX + "/test.txt";
        URL mockUrl = new URL("https://test-download-url.com/test.txt");
        when(s3Client.getUrl(eq(BUCKET_NAME), eq("test.txt"))).thenReturn(mockUrl);

        // Act
        String result = storageService.getDownloadUrl(fileUrl);

        // Assert
        assertEquals(mockUrl.toString(), result);
        verify(s3Client).getUrl(BUCKET_NAME, "test.txt");
    }

    @Test
    void uploadFile_WithMetadata() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test image data".getBytes()
        );

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // Act
        storageService.uploadFile(file);

        // Assert
        verify(s3Client).putObject(requestCaptor.capture());
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        
        assertEquals(BUCKET_NAME, capturedRequest.getBucketName());
        assertEquals("image/jpeg", capturedRequest.getMetadata().getContentType());
        assertEquals(file.getSize(), capturedRequest.getMetadata().getContentLength());
    }
} 