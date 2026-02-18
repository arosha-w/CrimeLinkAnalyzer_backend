package com.crimeLink.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Sender ID is required")
    private Integer senderId;

    @NotBlank(message = "Message type is required")
    private String messageType; // text, image, audio

    private String content;     // text content (required for text type)

    private String mediaUrl;    // Firebase Storage URL (required for image/audio type)
}
