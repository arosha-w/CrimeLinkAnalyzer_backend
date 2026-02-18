package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.ChatMessageDTO;
import com.crimeLink.analyzer.dto.SendMessageRequest;
import com.crimeLink.analyzer.entity.ChatMessage;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.ChatMessageRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDTO sendMessage(SendMessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Validate based on message type
        String type = request.getMessageType().toLowerCase();
        if ("text".equals(type)) {
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new RuntimeException("Content is required for text messages");
            }
        } else if ("image".equals(type) || "audio".equals(type)) {
            if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
                throw new RuntimeException("Media URL is required for " + type + " messages");
            }
        } else {
            throw new RuntimeException("Invalid message type: " + type + ". Must be text, image, or audio");
        }

        ChatMessage message = new ChatMessage();
        message.setSenderId(sender.getUserId());
        message.setSenderName(sender.getName());
        message.setSenderEmail(sender.getEmail());
        message.setMessageType(type);
        message.setContent(request.getContent());
        message.setMediaUrl(request.getMediaUrl());

        ChatMessage saved = chatMessageRepository.save(message);
        return toDTO(saved);
    }

    public List<ChatMessageDTO> getRecentMessages() {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByOrderByCreatedAtDesc();
        // Reverse so oldest is first (chronological order for display)
        Collections.reverse(messages);
        return messages.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getMessagesSince(LocalDateTime after) {
        return chatMessageRepository.findByCreatedAtAfterOrderByCreatedAtAsc(after)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO toDTO(ChatMessage msg) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(msg.getId());
        dto.setSenderId(msg.getSenderId());
        dto.setSenderName(msg.getSenderName());
        dto.setSenderEmail(msg.getSenderEmail());
        dto.setMessageType(msg.getMessageType());
        dto.setContent(msg.getContent());
        dto.setMediaUrl(msg.getMediaUrl());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }
}
