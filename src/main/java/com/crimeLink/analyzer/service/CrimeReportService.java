package com.crimeLink.analyzer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crimeLink.analyzer.dto.CrimeReportDTO;
import com.crimeLink.analyzer.entity.CrimeReport;
import com.crimeLink.analyzer.entity.CrimeType;
import com.crimeLink.analyzer.mapper.CrimeReportMapper;
import com.crimeLink.analyzer.repository.CrimeReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrimeReportService {

    private final CrimeReportRepository crimeReportRepository;

    public CrimeReportDTO saveCrimeReport(CrimeReportDTO reportDTO) {
        // if (reportDTO.getCrimeType() == null) {
        //     throw new IllegalArgumentException("Crime type cannot be null");
        // }
        // if (reportDTO.getLatitude() == null) {
        //     throw new IllegalArgumentException("Latitude cannot be null");
        // }
        // if (reportDTO.getLongitude() == null) {
        //     throw new IllegalArgumentException("Longitude cannot be null");
        // }
        // if (reportDTO.getDateReported() == null) {
        //     throw new IllegalArgumentException("Date reported cannot be null");
        // }
        // if (reportDTO.getTimeReported() == null) {
        //     throw new IllegalArgumentException("Time reported cannot be null");
        // }
        // if (reportDTO.getDescription() == null) {
        //     throw new IllegalArgumentException("Description cannot be null");
        // }

        CrimeReport report = CrimeReportMapper.mapToCrimeReport(reportDTO);
        CrimeReport savedReport = crimeReportRepository.save(report);
        return CrimeReportMapper.mapToCrimeReportDTO(savedReport);
    }

    public List<CrimeReportDTO> getAllCrimeReports(){
        List<CrimeReport> crimeReports = crimeReportRepository.findAll();
        return crimeReports.stream().map((report) -> CrimeReportMapper.mapToCrimeReportDTO(report)).collect(Collectors.toList());
    }

    public CrimeReportDTO getCrimeReportById(Long reportId){
        CrimeReport report = crimeReportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        return CrimeReportMapper.mapToCrimeReportDTO(report);
    }
}
