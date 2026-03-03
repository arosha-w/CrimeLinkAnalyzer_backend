package com.crimeLink.analyzer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crimeLink.analyzer.dto.CrimeLocationDTO;
import com.crimeLink.analyzer.dto.CrimeReportDTO;
import com.crimeLink.analyzer.dto.EvidenceDTO;
import com.crimeLink.analyzer.entity.CrimeReport;
import com.crimeLink.analyzer.mapper.CrimeReportMapper;
import com.crimeLink.analyzer.repository.CrimeReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrimeReportService {

    private final CrimeReportRepository crimeReportRepository;
    private final SupabaseService supabaseService;

    public CrimeReportDTO saveCrimeReport(CrimeReportDTO reportDTO) {
        // if (reportDTO.getCrimeType() == null) {
        // throw new IllegalArgumentException("Crime type cannot be null");
        // }
        // if (reportDTO.getLatitude() == null) {
        // throw new IllegalArgumentException("Latitude cannot be null");
        // }
        // if (reportDTO.getLongitude() == null) {
        // throw new IllegalArgumentException("Longitude cannot be null");
        // }
        // if (reportDTO.getDateReported() == null) {
        // throw new IllegalArgumentException("Date reported cannot be null");
        // }
        // if (reportDTO.getTimeReported() == null) {
        // throw new IllegalArgumentException("Time reported cannot be null");
        // }
        // if (reportDTO.getDescription() == null) {
        // throw new IllegalArgumentException("Description cannot be null");
        // }

        CrimeReport report = CrimeReportMapper.mapToCrimeReport(reportDTO);
        CrimeReport savedReport = crimeReportRepository.save(report);
        return CrimeReportMapper.mapToCrimeReportDTO(savedReport);
    }

    public List<CrimeReportDTO> getAllCrimeReports() {
        return crimeReportRepository.findAll().stream().map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    private CrimeReportDTO convertToListDTO(CrimeReport report) {
        CrimeReportDTO dto = new CrimeReportDTO();
        dto.setReportId(report.getReportId());
        dto.setLongitude(report.getLongitude());
        dto.setLatitude(report.getLatitude());
        dto.setDescription(report.getDescription());
        dto.setDateReported(report.getDateReported());
        dto.setTimeReported(report.getTimeReported());
        dto.setCrimeType(report.getCrimeType() != null ? report.getCrimeType().name() : null);
        dto.setEvidences(List.of());
        return dto;
    }

    public CrimeReportDTO getCrimeReportById(Long reportId) {
        CrimeReport report = crimeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return convertToDTOWithSignedUrls(report);
    }

    private CrimeReportDTO convertToDTOWithSignedUrls(CrimeReport report) {
        CrimeReportDTO dto = CrimeReportMapper.mapToCrimeReportDTO(report);

        if (report.getEvidences() != null) {
            List<EvidenceDTO> evidenceDTOs = report.getEvidences().stream()
                    .map(e -> EvidenceDTO.builder().evidenceId(e.getId()).fileName(e.getFileName())
                            .fileType(e.getFileType()).fileSize(e.getFileSize())
                            .downloadUrl(supabaseService.getFileUrl(e.getFileName())).build())
                    .collect(Collectors.toList());

            dto.setEvidences(evidenceDTOs);
        }
        return dto;
    }

    public List<CrimeLocationDTO> getCrimeMapLocations() {
        return crimeReportRepository.findCrimeLocations();
    }
}
