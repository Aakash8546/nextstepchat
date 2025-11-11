package com.nextstep.chat.controller;

import com.nextstep.chat.dto.MessageDTO;
import com.nextstep.chat.model.MessageType;
import com.nextstep.chat.service.FileStorageService;
import com.nextstep.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService fileStorageService;
    private final MessageService messageService;

    @PostMapping("/upload/image")
    public ResponseEntity<MessageDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long chatRoomId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId) throws IOException {

        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        MessageDTO message = messageService.sendFileMessage(
                chatRoomId, senderId, receiverId, file, MessageType.IMAGE
        );
        return ResponseEntity.ok(message);
    }

    @PostMapping("/upload/pdf")
    public ResponseEntity<MessageDTO> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long chatRoomId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId) throws IOException {

        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().build();
        }

        MessageDTO message = messageService.sendFileMessage(
                chatRoomId, senderId, receiverId, file, MessageType.PDF
        );
        return ResponseEntity.ok(message);
    }

    @PostMapping("/upload/document")
    public ResponseEntity<MessageDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long chatRoomId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId) throws IOException {

        MessageDTO message = messageService.sendFileMessage(
                chatRoomId, senderId, receiverId, file, MessageType.DOCUMENT
        );
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        byte[] fileData = fileStorageService.loadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
