package com.chatgenius.service;

import com.chatgenius.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    /**
     * Upload a file and create an attachment for a message
     * @param messageId The ID of the message to attach the file to
     * @param file The file to upload
     * @return The created attachment
     */
    Attachment uploadAttachment(UUID messageId, MultipartFile file);

    /**
     * Get all attachments for a message
     * @param messageId The ID of the message
     * @return List of attachments
     */
    List<Attachment> getMessageAttachments(UUID messageId);

    /**
     * Delete an attachment
     * @param attachmentId The ID of the attachment to delete
     */
    void deleteAttachment(UUID attachmentId);

    /**
     * Get an attachment by ID
     * @param attachmentId The ID of the attachment
     * @return The attachment
     */
    Attachment getAttachment(UUID attachmentId);
} 