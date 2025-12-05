package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DutyScheduleService {

    private final DutyScheduleRepository dutyRepo;
    private final UserRepository userRepo;
    private final OfficerPerformanceRepository performanceRepo;

    /**
     * Returns one or more rows per officer for the given date.
     * If the officer has no duties that day → one empty editable row.
     */
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
                        "",      // status
                        ""       // description
                ));
            } else {
                // one row per duty
                for (DutySchedule duty : officerDuties) {
                    rows.add(new OfficerDutyRowDTO(
                            officer.getUserId(),
                            officer.getName(),
                            duty.getLocation() != null ? duty.getLocation() : "",
                            duty.getTimeRange() != null ? duty.getTimeRange() : "",
                            duty.getStatus() != null ? duty.getStatus() : "",
                            duty.getDescription() != null ? duty.getDescription() : ""
                    ));
                }
            }
        }

        return rows;
    }


    public DutySchedule saveDuty(DutyScheduleRequest req) {

        if (req.getOfficerId() == null) {
            throw new IllegalArgumentException("OfficerId is required");
        }
        if (req.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        User officer = userRepo.findById(req.getOfficerId())
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        DutySchedule duty = new DutySchedule();
        duty.setAssignedOfficer(officer);
        duty.setDate(req.getDate());
        // duty.setDuration(req.getDuration() != null ? req.getDuration() : 240);
        // duty.setTaskType(req.getTaskType() != null ? req.getTaskType() : "General");
        duty.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        duty.setLocation(req.getLocation());
        duty.setDescription(req.getDescription());
        duty.setTimeRange(req.getTimeRange());

        DutySchedule saved = dutyRepo.save(duty);

        // Update performance table
        updateOfficerPerformanceAfterDuty(officer, saved);

        return saved;
    }

    /**
     * Update or initialize OfficerPerformance after saving a duty.
     * FIXED: uses List<OfficerPerformance> to avoid NonUniqueResultException.
     */
    private void updateOfficerPerformanceAfterDuty(User officer, DutySchedule duty) {

        // repository must be: List<OfficerPerformance> findByOfficer_UserId(Integer userId);
        List<OfficerPerformance> perfList =
                performanceRepo.findByOfficer_UserId(officer.getUserId());

        OfficerPerformance perf;

        if (perfList.isEmpty()) {
            // create new record
            perf = OfficerPerformance.builder()
                    .officer(officer)
                    .totalDuties(0)
                    .reliabilityScore(60)
                    .availabilityStatus("Available")
                    .build();
        } else {
            // use first existing record (if duplicates, DB cleanup can be done later)
            perf = perfList.get(0);
        }

        int total = perf.getTotalDuties() == null ? 0 : perf.getTotalDuties();
        perf.setTotalDuties(total + 1);
        perf.setLastDutyDate(duty.getDate());
        perf.setLastUpdated(LocalDateTime.now());

        performanceRepo.save(perf);
    }

    /**
     * Fetch duties between two dates (inclusive).
     */
    public List<DutySchedule> getDutiesBetween(LocalDate start, LocalDate end) {
        return dutyRepo.findByDateBetween(start, end);
    }

    /**
     * Generate a PDF report for all duties between start and end dates.
     */
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

                // Other fields
                table.addCell(duty.getLocation() != null ? duty.getLocation() : "");
                table.addCell(duty.getStatus() != null ? duty.getStatus() : "");
                table.addCell(duty.getDescription() != null ? duty.getDescription() : "");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating Duty Schedule PDF", e);
        }
    }

    /**
     * Helper mapping one DutySchedule → OfficerDutyRowDTO
     * (Not used above yet, but you can reuse it if needed.)
     */
    private OfficerDutyRowDTO toRow(DutySchedule duty) {

        User assignedOfficer = duty.getAssignedOfficer();
        Integer officerId = assignedOfficer != null ? assignedOfficer.getUserId() : null;
        String officerName = assignedOfficer != null ? assignedOfficer.getName() : "";

        return new OfficerDutyRowDTO(
                officerId,
                officerName,
                duty.getLocation(),
                duty.getTimeRange(),
                duty.getStatus(),
                duty.getDescription()
        );
    }
}
