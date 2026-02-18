package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.ChatMessageDTO;
import com.crimeLink.analyzer.dto.SendMessageRequest;
import com.crimeLink.analyzer.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        ChatMessageDTO result = chatService.sendMessage(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatMessageDTO>> getRecentMessages() {
        List<ChatMessageDTO> messages = chatService.getRecentMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/since")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesSince(
            @RequestParam String after) {
        LocalDateTime afterTime = LocalDateTime.parse(after);
        List<ChatMessageDTO> messages = chatService.getMessagesSince(afterTime);
        return ResponseEntity.ok(messages);
    }
}
