package com.crimeLink.analyzer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvidenceDTO {
    private UUID evidenceId;
    private String fileName;
    private String fileType;
    private Long fileSize;

    private String downloadUrl;
}
