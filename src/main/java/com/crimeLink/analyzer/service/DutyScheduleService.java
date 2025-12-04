package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DutyScheduleService {

    private final DutyScheduleRepository dutyRepo;
    private final UserRepository userRepo;

    public List<OfficerDutyRowDTO> getOfficerRowsForDate(LocalDate date) {

        List<User> officers = userRepo.findByRoleAndStatus("FieldOfficer", "Active");

        List<DutySchedule> dutiesForDate =
                dutyRepo.findByDate(date); // NEW CALL

        List<OfficerDutyRowDTO> rows = new ArrayList<>();

        for (User officer : officers) {

            List<DutySchedule> officerDuties =
                    dutiesForDate.stream()
                            .filter(d -> d.getAssignedOfficer().getUserId().equals(officer.getUserId()))
                            .toList();

            if (officerDuties.isEmpty()) {
                rows.add(new OfficerDutyRowDTO(
                        officer.getUserId(),
                        officer.getName(),
                        "", "", "", ""
                ));
            } else {
                for (DutySchedule duty : officerDuties) {
                    rows.add(new OfficerDutyRowDTO(
                            officer.getUserId(),
                            officer.getName(),
                            duty.getLocation(),
                            duty.getTimeRange(),
                            duty.getStatus(),
                            duty.getDescription()
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

        // ✅ SET DATE
        duty.setDate(req.getDate());

        //duty.setDuration(req.getDuration() != null ? req.getDuration() : 240);
        //duty.setTaskType(req.getTaskType() != null ? req.getTaskType() : "General");
        duty.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        duty.setLocation(req.getLocation());
        duty.setDescription(req.getDescription());
        duty.setTimeRange(req.getTimeRange());

        return dutyRepo.save(duty);
    }


    public List<DutySchedule> getDutiesBetween(LocalDate start, LocalDate end) {
        return dutyRepo.findByDateBetween(start, end);
    }
    // ✅ NEW: Generate PDF bytes for a given date range
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

            // Table with columns: Date, Time, Officer, Location, Status, Description
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
                // Adjust these getter calls according to your actual entity fields
                table.addCell(duty.getDate() != null ? duty.getDate().format(fmt) : "");

                // assuming you store "06:00-21:00" in a String field timeRange
                table.addCell(duty.getTimeRange() != null ? duty.getTimeRange() : "");

                // assuming DutySchedule has assignedOfficer: User
                String officerName = "";
                if (duty.getAssignedOfficer() != null) {
                    officerName = duty.getAssignedOfficer().getName();
                }
                table.addCell(officerName);

                table.addCell(duty.getLocation() != null ? duty.getLocation() : "");
                table.addCell(duty.getStatus() != null ? duty.getStatus() : "");
                table.addCell(duty.getDescription() != null ? duty.getDescription() : "");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (DocumentException e) {
            // log properly in real code
            throw new RuntimeException("Error generating Duty Schedule PDF", e);
        }
    }
    private OfficerDutyRowDTO toRow(DutySchedule duty) {
        String officerName = duty.getAssignedOfficer() != null
                ? duty.getAssignedOfficer().getName()
                : "";

        return new OfficerDutyRowDTO(
                duty.getAssignedOfficer() != null ? duty.getAssignedOfficer().getUserId() : null,
                officerName,
                duty.getLocation(),
                duty.getTimeRange(),
                duty.getStatus(),
                duty.getDescription()
        );
    }

}
