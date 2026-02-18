package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Integer senderId;
    private String senderName;
    private String senderEmail;
    private String messageType;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
}
