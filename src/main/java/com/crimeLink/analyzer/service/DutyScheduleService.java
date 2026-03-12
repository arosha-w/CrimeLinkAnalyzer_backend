package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.DutyStatus;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DutyScheduleService {

    private final DutyScheduleRepository dutyRepo;
    private final UserRepository userRepo;
    private final OfficerPerformanceRepository performanceRepo;

    private static final List<String> DEFAULT_DUTY_LOCATIONS = List.of(
            "Matara", "Hakmana", "Weligama", "Akuressa"
    );

    public DutyScheduleService(DutyScheduleRepository dutyRepo, UserRepository userRepo, OfficerPerformanceRepository performanceRepo) {
        this.dutyRepo = dutyRepo;
        this.userRepo = userRepo;
        this.performanceRepo = performanceRepo;
    }

    // Load officer rows for a given date (used by frontend table)
    public List<OfficerDutyRowDTO> getOfficerRowsForDate(LocalDate date) {

        // 1) All ACTIVE field officers
        List<User> officers = userRepo.findByRoleAndStatus("FieldOfficer", "Active");

        // 2) All duties for that date
        List<DutySchedule> dutiesForDate = dutyRepo.findByDate(date);

        List<OfficerDutyRowDTO> rows = new ArrayList<>();

        for (User officer : officers) {
            // filter duties for this officer
            List<DutySchedule> officerDuties = dutiesForDate.stream()
                    .filter(d -> d.getAssignedOfficer() != null
                            && d.getAssignedOfficer().getUserId().equals(officer.getUserId()))
                    .toList();

            if (officerDuties.isEmpty()) {
                // no duty yet for this officer on that date → empty row
                rows.add(new OfficerDutyRowDTO(
                        officer.getUserId(),
                        officer.getName(),
                        "",      // location
                        "",      // timeRange
                        "",      // status (no duty assigned)
                        ""       // description
                ));
            } else {
                // one row per duty
                for (DutySchedule duty : officerDuties) {
                    String statusString = (duty.getStatus() != null)
                            ? duty.getStatus().name()  // enum -> "ACTIVE"/"COMPLETED"/"ABSENT"
                            : "";

                    rows.add(new OfficerDutyRowDTO(
                            officer.getUserId(),
                            officer.getName(),
                            duty.getLocation() != null ? duty.getLocation() : "",
                            duty.getTimeRange() != null ? duty.getTimeRange() : "",
                            statusString,
                            duty.getDescription() != null ? duty.getDescription() : ""
                    ));
                }
            }
        }

        return rows;
    }
    // Create / Update (Upsert) a Duty
    //  - ABSENT  => can be saved without location/timeRange
    //  - ACTIVE/COMPLETED => require timeRange and use upsert
    public DutySchedule createDuty(DutyScheduleRequest req) {

        if (req.getOfficerId() == null) {
            throw new IllegalArgumentException("OfficerId is required");
        }
        if (req.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (req.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }

        User officer = userRepo.findById(req.getOfficerId())
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        DutyStatus status = req.getStatus();
        DutySchedule duty;
        boolean isNew = false;

        if (status == DutyStatus.Absent) {
            //  For ABSENT: allow saving even with no timeRange/location
            duty = dutyRepo
                    .findByDateAndAssignedOfficer_UserId(req.getDate(), officer.getUserId())
                    .orElse(null);

            if (duty == null) {
                duty = new DutySchedule();
                duty.setAssignedOfficer(officer);
                duty.setDate(req.getDate());
                isNew = true; // first time marking absent
            }
            duty.setAssignedOfficer(officer);
            duty.setDate(req.getDate());
            duty.setStatus(DutyStatus.Absent);
            duty.setLocation(req.getLocation());         // can be null
            duty.setTimeRange(req.getTimeRange());      // can be null
            duty.setDescription(req.getDescription());  // can be null
            isNew = true; // count as a new record for performance
        } else {
            //  For ACTIVE / COMPLETED: require timeRange and do upsert
            String tr = req.getTimeRange();
            if (tr == null || tr.isBlank()) {
                throw new IllegalArgumentException("Time range is required for non-ABSENT status");
            }

            // 1) Try to find existing duty for SAME date + officer + timeRange
            duty = dutyRepo
                    .findByDateAndAssignedOfficer_UserIdAndTimeRange(
                            req.getDate(),
                            officer.getUserId(),
                            tr
                    )
                    .orElse(null);

            if (duty == null) {
                // 2) No existing record → create new
                duty = new DutySchedule();
                duty.setAssignedOfficer(officer);
                duty.setDate(req.getDate());
                isNew = true;
            }

            // 3) Update fields from request
            duty.setStatus(status);
            duty.setLocation(req.getLocation());
            duty.setTimeRange(tr);
            duty.setDescription(req.getDescription());
        }

        DutySchedule saved = dutyRepo.save(duty);

    //  Only count duties with ACTIVE or COMPLETED status
        if (isNew && (saved.getStatus() == DutyStatus.Active || saved.getStatus() == DutyStatus.Completed)) {
            updateOfficerPerformanceAfterDuty(officer, saved);
        }

        return saved;

    }

    // Bulk create/update duties
    public void createDuties(List<DutyScheduleRequest> requests) {
        for (DutyScheduleRequest req : requests) {
            createDuty(req);
        }
    }
    // Update only the status of an existing duty
    @Transactional
    public void updateDutyStatus(Long id, DutyStatus status) {
        DutySchedule duty = dutyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Duty not found"));

        duty.setStatus(status);
        dutyRepo.save(duty);
    }
    // Performance table update
    private void updateOfficerPerformanceAfterDuty(User officer, DutySchedule duty) {

        // repository must be: List<OfficerPerformance> findByOfficer_UserId(Integer userId);
        if (duty.getStatus() == DutyStatus.Absent) return;

        List<OfficerPerformance> perfList =
                performanceRepo.findByOfficer_UserId(officer.getUserId());

        OfficerPerformance perf;

        if (duty.getStatus() == DutyStatus.Absent) {
            return;
        }

        if (perfList.isEmpty()) {
            // create new record
            perf = OfficerPerformance.builder()
                    .officer(officer)
                    .totalDuties(0)
                    .reliabilityScore(50)

                    .build();
        } else {
            // use first existing record (if duplicates, DB cleanup can be done later)
            perf = perfList.get(0);
        }

        // 2) Current values (with safe defaults)
        int currentTotalDuties = perf.getTotalDuties() == null ? 0 : perf.getTotalDuties();
        int currentReliability = perf.getReliabilityScore() == null ? 50 : perf.getReliabilityScore();

        // 3) Update based on duty status
        if (duty.getStatus() == DutyStatus.Completed || duty.getStatus() == DutyStatus.Active) {
            // Only Active/Completed count as duties in performance
            perf.setTotalDuties(currentTotalDuties + 1);
        }

        int newReliability = currentReliability;

        if (duty.getStatus() == DutyStatus.Completed) {
            // completed duty → small reward
            newReliability = Math.min(100, currentReliability + 2);
        } else if (duty.getStatus() == DutyStatus.Absent) {
            // absent → bigger penalty
            newReliability = Math.max(0, currentReliability - 5);
        }
        perf.setReliabilityScore(newReliability);

        perf.setLastDutyDate(duty.getDate());
        perf.setLastUpdated(LocalDateTime.now());

        performanceRepo.save(perf);
    }
    public List<String> getDutyLocations() {
        List<String> dbLocations = dutyRepo.findDistinctLocations();
        return dbLocations.isEmpty() ? DEFAULT_DUTY_LOCATIONS : dbLocations;
    }
    // Range queries & PDF
    public List<DutySchedule> getDutiesBetween(LocalDate start, LocalDate end) {
        return dutyRepo.findByDateBetween(start, end);
    }

    public byte[] generateDutyScheduleReportPdf(LocalDate start, LocalDate end) {

        List<DutySchedule> duties = getDutiesBetween(start, end);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Title
            document.add(new Paragraph("Duty Schedule Report"));
            document.add(new Paragraph(
                    "Date range: " + start.format(fmt) + " to " + end.format(fmt)
            ));
            document.add(new Paragraph(" ")); // empty line

            // Table with columns: Date, Time Range, Officer, Location, Status, Description
            PdfPTable table = new PdfPTable(6); // 6 columns
            table.setWidthPercentage(100);

            // Header row
            table.addCell("Date");
            table.addCell("Time Range");
            table.addCell("Officer");
            table.addCell("Location");
            table.addCell("Status");
            table.addCell("Description");

            // Data rows
            for (DutySchedule duty : duties) {
                // Date
                table.addCell(duty.getDate() != null ? duty.getDate().format(fmt) : "");

                // Time range stored as String (e.g. "06:00-21:00")
                table.addCell(duty.getTimeRange() != null ? duty.getTimeRange() : "");

                // Officer name
                String officerName = "";
                if (duty.getAssignedOfficer() != null) {
                    officerName = duty.getAssignedOfficer().getName();
                }
                table.addCell(officerName);

                // Location
                table.addCell(duty.getLocation() != null ? duty.getLocation() : "");

                // Status (enum -> String)
                String statusString = duty.getStatus() != null
                        ? duty.getStatus().name()
                        : "";
                table.addCell(statusString);

                // Description
                table.addCell(duty.getDescription() != null ? duty.getDescription() : "");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating Duty Schedule PDF", e);
        }
    }

    // (Helper) Convert entity to DTO if needed elsewhere
    private OfficerDutyRowDTO toRow(DutySchedule duty) {

        User assignedOfficer = duty.getAssignedOfficer();
        Integer officerId = assignedOfficer != null ? assignedOfficer.getUserId() : null;
        String officerName = assignedOfficer != null ? assignedOfficer.getName() : "";

        String statusString = duty.getStatus() != null
                ? duty.getStatus().name()
                : "";

        return new OfficerDutyRowDTO(
                officerId,
                officerName,
                duty.getLocation(),
                duty.getTimeRange(),
                statusString,
                duty.getDescription()
        );
    }
}
