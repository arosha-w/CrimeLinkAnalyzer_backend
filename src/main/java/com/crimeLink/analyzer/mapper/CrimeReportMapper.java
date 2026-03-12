package com.crimeLink.analyzer.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.crimeLink.analyzer.dto.CrimeReportDTO;
import com.crimeLink.analyzer.dto.EvidenceDTO;
import com.crimeLink.analyzer.entity.CrimeReport;
import com.crimeLink.analyzer.entity.CrimeType;

public class CrimeReportMapper {
    public static CrimeReport mapToCrimeReport(CrimeReportDTO dto) {

        CrimeReport report = new CrimeReport();

        report.setLongitude(dto.getLongitude());
        report.setLatitude(dto.getLatitude());
        report.setDescription(dto.getDescription());
        report.setDateReported(dto.getDateReported());
        report.setTimeReported(dto.getTimeReported());
        report.setCrimeType(CrimeType.valueOf(dto.getCrimeType()));

        return report;
    }

    public static CrimeReportDTO mapToCrimeReportDTO(CrimeReport entity) {
        CrimeReportDTO dto = new CrimeReportDTO();

        dto.setReportId(entity.getReportId());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setDescription(entity.getDescription());
        dto.setDateReported(entity.getDateReported());
        dto.setTimeReported(entity.getTimeReported());
        dto.setCrimeType(entity.getCrimeType().name());

        if (entity.getEvidences() != null) {
            List<EvidenceDTO> evidenceDTOs = entity.getEvidences().stream().map(e -> new EvidenceDTO(
                    e.getId(),
                    e.getFileName(),
                    e.getFileType(),
                    e.getFileSize(),
                    null)).collect(Collectors.toList());
            dto.setEvidences(evidenceDTOs);
        }

        return dto;
    }
}
