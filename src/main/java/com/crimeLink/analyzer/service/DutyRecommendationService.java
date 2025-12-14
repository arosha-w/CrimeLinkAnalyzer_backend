package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyRecommendationRequest;
import com.crimeLink.analyzer.dto.OfficerRecommendationDTO;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DutyRecommendationService {

    private final UserRepository userRepo;
    private final OfficerPerformanceRepository performanceRepo;

    public DutyRecommendationService(UserRepository userRepo, OfficerPerformanceRepository performanceRepo) {
        this.userRepo = userRepo;
        this.performanceRepo = performanceRepo;
    }
    /**
     * Main entry: get ranked officer recommendations for a given request.
     * - Favors officers with LOWER workload (less total duties)
     * - Considers recency and reliability
     */
    public List<OfficerRecommendationDTO> getOfficerRecommendations(DutyRecommendationRequest req) {

        // 1) All ACTIVE field officers
        List<User> officers = userRepo.findByRoleAndStatus("FieldOfficer", "Active");

        // 2) Map officer → score + explanation
        List<OfficerRecommendationDTO> scored = officers.stream()
                .map(officer -> {
                    // performanceRepo returns LIST (not Optional)
                    List<OfficerPerformance> list =
                            performanceRepo.findByOfficer_UserId(officer.getUserId());
                    OfficerPerformance perf = list.isEmpty() ? null : list.get(0);

                    double score = calculateScore(officer, perf, req);
                    String reason = buildReason(officer, perf, req, score);

                    String badgeNo = null; // set from officer if you have a badge field

                    // since availability & preferredLocation are removed from entity,
                    // we just return defaults for DTO fields:
                    String availabilityStatus = "Unknown";
                    boolean locationMatch = false;

                    return new OfficerRecommendationDTO(
                            officer.getUserId(),                            // officerId
                            officer.getName(),                             // name
                            badgeNo,                                      // badgeNo
                            score,                                        // recommendationScore
                            availabilityStatus,                           // availabilityStatus (DTO field)
                            perf != null ? perf.getLastDutyDate() : null, // lastDutyDate
                            perf != null && perf.getTotalDuties() != null
                                    ? perf.getTotalDuties()
                                    : 0,                                   // totalDuties
                            locationMatch,                                // locationMatch
                            reason                                        // reason
                    );
                })
                // highest score first
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

    /**
     * Final score = weighted combination of:
     * - Workload (strong weight)   => fewer duties → higher score
     * - Recency (when last duty was done)
     * - Reliability (from performance table)
     */
    private double calculateScore(User officer, OfficerPerformance perf, DutyRecommendationRequest req) {

        double workloadWeight     = 0.70; // strongly prefer low workload
        double recencyWeight      = 0.20;
        double reliabilityWeight  = 0.10;

        double workloadScore     = calcWorkloadScore(perf);

        // If DutyRecommendationRequest.date is a LocalDate:
        LocalDate targetDate     = req.getDate();
        // If it is a String, use: LocalDate targetDate = LocalDate.parse(req.getDate());

        double recencyScore      = calcRecencyScore(perf, targetDate);
        double reliabilityScore  = calcReliabilityScore(perf);

        return workloadWeight * workloadScore
                + recencyWeight * recencyScore
                + reliabilityWeight * reliabilityScore;
    }
    /**
     * Workload score:
     * - Very few duties (≤ 5)   → 100 (strongly recommended)
     * - Moderate (6–15)         → 80
     * - Heavy (16–30)           → 50
     * - Very heavy (> 30)       → 20
     *
     * This makes low-duty officers float to the top of the list.
     */
    private double calcWorkloadScore(OfficerPerformance perf) {
        if (perf == null || perf.getTotalDuties() == null) return 100.0; // no history → treat as free
        int total = perf.getTotalDuties();

        if (total <= 5)   return 100.0; // very low load
        if (total <= 10)  return 80.0;  // moderate
        if (total <= 15)  return 50.0;  // high
        return 20.0;                    // very high workload
    }
    /**
     * Recency score:
     * - Officer hasn't worked for ≥ 7 days → 100 (very available)
     * - 3–6 days ago                       → 70
     * - 1–2 days ago                       → 40
     * - Same day / very recent             → 20
     */
    private double calcRecencyScore(OfficerPerformance perf, LocalDate targetDate) {
        if (perf == null || perf.getLastDutyDate() == null || targetDate == null) return 80.0;
        long days = ChronoUnit.DAYS.between(perf.getLastDutyDate(), targetDate);
        if (days >= 7) return 100.0;
        if (days >= 3) return 70.0;
        if (days >= 1) return 40.0;
        return 20.0;
    }
    /**
     * Reliability score is taken directly from performance table (0–100).
     */
    private double calcReliabilityScore(OfficerPerformance perf) {
        if (perf == null || perf.getReliabilityScore() == null) return 60.0;
        return perf.getReliabilityScore();
    }

    /**
     * Human-readable reason string for UI.
     */
    private String buildReason(User officer, OfficerPerformance perf,
                               DutyRecommendationRequest req, double score) {

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(String.format("%.1f", score)).append(", ");

        if (perf == null) {
            sb.append("No historical data; treated as low workload and neutral reliability.");
            return sb.toString();
        }

        sb.append("Total duties: ").append(perf.getTotalDuties()).append(",  ");

        if (perf.getLastDutyDate() != null) {
            sb.append("Last duty on ").append(perf.getLastDutyDate()).append(",  ");
        }

        if (perf.getReliabilityScore() != null) {
            sb.append("Reliability: ").append(perf.getReliabilityScore()).append("  ");
        }

        return sb.toString();
    }
}
