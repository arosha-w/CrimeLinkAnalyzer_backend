package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyRecommendationRequest;
import com.crimeLink.analyzer.dto.OfficerRecommendationDTO;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DutyRecommendationService {

    private final UserRepository userRepo;
    private final OfficerPerformanceRepository performanceRepo;

    public List<OfficerRecommendationDTO> getOfficerRecommendations(DutyRecommendationRequest req) {

        // 1) All ACTIVE field officers
        List<User> officers = userRepo.findByRoleAndStatus("FieldOfficer", "Active");

        // 2) Map officer → score + explanation
        List<OfficerRecommendationDTO> scored = officers.stream()
                .map(officer -> {
                    // 🔧 FIX: repository returns LIST, not Optional
                    List<OfficerPerformance> list =
                            performanceRepo.findByOfficer_UserId(officer.getUserId());
                    OfficerPerformance perf = list.isEmpty() ? null : list.get(0);

                    double score = calculateScore(officer, perf, req);
                    String reason = buildReason(officer, perf, req, score);

                    String badgeNo = null; // set from officer if you have this field

                    return new OfficerRecommendationDTO(
                            officer.getUserId(),
                            officer.getName(),
                            badgeNo,
                            score,
                            perf != null ? perf.getAvailabilityStatus() : "Unknown",
                            perf != null ? perf.getLastDutyDate() : null,
                            perf != null ? perf.getTotalDuties() : 0,
                            isLocationMatch(perf, req.getLocation()),
                            reason
                    );
                })
                .sorted(Comparator.comparingDouble(OfficerRecommendationDTO::getRecommendationScore).reversed())
                .collect(Collectors.toList());

        // 3) Limit to requiredOfficers if provided
        Integer required = req.getRequiredOfficers();
        if (required != null && required > 0 && required < scored.size()) {
            return scored.subList(0, required);
        }

        return scored;
    }

    // ---------- Scoring helpers ----------

    private double calculateScore(User officer, OfficerPerformance perf, DutyRecommendationRequest req) {

        double workloadWeight = 0.30;
        double availabilityWeight = 0.25;
        double locationWeight = 0.20;
        double recencyWeight = 0.15;
        double reliabilityWeight = 0.10;

        double workloadScore = calcWorkloadScore(perf);
        double availabilityScore = calcAvailabilityScore(perf);
        double locationScore = calcLocationScore(perf, req.getLocation());
        double recencyScore = calcRecencyScore(perf, req.getDate());
        double reliabilityScore = calcReliabilityScore(perf);

        return workloadWeight * workloadScore
                + availabilityWeight * availabilityScore
                + locationWeight * locationScore
                + recencyWeight * recencyScore
                + reliabilityWeight * reliabilityScore;
    }

    private double calcWorkloadScore(OfficerPerformance perf) {
        if (perf == null || perf.getTotalDuties() == null) return 100.0;
        int total = perf.getTotalDuties();
        if (total <= 5) return 100.0;
        if (total <= 15) return 70.0;
        if (total <= 30) return 40.0;
        return 20.0;
    }

    private double calcAvailabilityScore(OfficerPerformance perf) {
        if (perf == null || perf.getAvailabilityStatus() == null) return 50.0;
        return switch (perf.getAvailabilityStatus()) {
            case "Available" -> 100.0;
            case "OnDuty" -> 40.0;
            case "OnLeave" -> 10.0;
            case "Unavailable" -> 0.0;
            default -> 50.0;
        };
    }

    private boolean isLocationMatch(OfficerPerformance perf, String location) {
        if (perf == null || perf.getPreferredLocations() == null || location == null) return false;
        return perf.getPreferredLocations().toLowerCase().contains(location.toLowerCase());
    }

    private double calcLocationScore(OfficerPerformance perf, String location) {
        return isLocationMatch(perf, location) ? 100.0 : 50.0;
    }

    private double calcRecencyScore(OfficerPerformance perf, LocalDate targetDate) {
        if (perf == null || perf.getLastDutyDate() == null || targetDate == null) return 80.0;
        long days = ChronoUnit.DAYS.between(perf.getLastDutyDate(), targetDate);
        if (days >= 7) return 100.0;
        if (days >= 3) return 70.0;
        if (days >= 1) return 40.0;
        return 20.0;
    }

    private double calcReliabilityScore(OfficerPerformance perf) {
        if (perf == null || perf.getReliabilityScore() == null) return 60.0;
        return perf.getReliabilityScore();
    }

    private String buildReason(User officer, OfficerPerformance perf,
                               DutyRecommendationRequest req, double score) {

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(String.format("%.1f", score)).append(". ");

        if (perf == null) {
            sb.append("No historical data; treated as neutral.");
            return sb.toString();
        }

        if (isLocationMatch(perf, req.getLocation())) {
            sb.append("Experienced in ").append(req.getLocation()).append(". ");
        }

        sb.append("Total duties: ").append(perf.getTotalDuties()).append(". ");
        sb.append("Availability: ").append(perf.getAvailabilityStatus()).append(". ");

        return sb.toString();
    }
}
