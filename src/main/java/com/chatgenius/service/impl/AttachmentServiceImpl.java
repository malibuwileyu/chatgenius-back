package com.chatgenius.service.impl;

import com.chatgenius.exception.ResourceNotFoundException;
import com.chatgenius.exception.ValidationException;
import com.chatgenius.model.Attachment;
import com.chatgenius.model.Message;
import com.chatgenius.repository.AttachmentRepository;
import com.chatgenius.repository.MessageRepository;
import com.chatgenius.service.AttachmentService;
import com.chatgenius.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private final AttachmentRepository attachmentRepository;
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public Attachment uploadAttachment(UUID messageId, MultipartFile file) {
        logger.debug("Uploading attachment for message: {}", messageId);
        
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message", messageId.toString()));

        try {
            // Upload file to storage service
            String fileUrl = storageService.uploadFile(file);

            // Create attachment record
            Attachment attachment = new Attachment();
            attachment.setMessage(message);
            attachment.setFileUrl(fileUrl);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFileSize((int) file.getSize());

            attachment = attachmentRepository.save(attachment);
            logger.info("Successfully uploaded attachment: {}", attachment.getId());
            return attachment;
        } catch (Exception e) {
            logger.error("Failed to upload attachment", e);
            throw new RuntimeException("Failed to upload attachment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attachment> getMessageAttachments(UUID messageId) {
        logger.debug("Getting attachments for message: {}", messageId);
        
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("Message", messageId.toString());
        }

        return attachmentRepository.findByMessageId(messageId);
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        logger.debug("Deleting attachment: {}", attachmentId);
        
        Attachment attachment = getAttachment(attachmentId);

        try {
            // Delete from storage service
            storageService.deleteFile(attachment.getFileUrl());

            // Delete from database
            attachmentRepository.delete(attachment);
            logger.info("Successfully deleted attachment: {}", attachmentId);
        } catch (Exception e) {
            logger.error("Failed to delete attachment", e);
            throw new RuntimeException("Failed to delete attachment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Attachment getAttachment(UUID attachmentId) {
        logger.debug("Getting attachment: {}", attachmentId);
        
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId.toString()));
    }
} 