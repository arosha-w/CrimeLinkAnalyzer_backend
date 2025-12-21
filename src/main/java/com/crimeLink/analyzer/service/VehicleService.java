package com.crimeLink.analyzer.service;


import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.repository.VehicleRepository;
import com.lowagie.text.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;


@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle addVehicle(Vehicle vehicle) {

        if (vehicle.getLostDate() == null || vehicle.getLostDate().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            vehicle.setLostDate(LocalDateTime.now().format(formatter));

        }
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

        if (optionalVehicle.isPresent()) {
            Vehicle vehicle = optionalVehicle.get();

            vehicle.setNumberPlate(vehicleDetails.getNumberPlate());
            vehicle.setOwnerName(vehicleDetails.getOwnerName());
            vehicle.setVehicleType(vehicleDetails.getVehicleType());
            vehicle.setStatus(vehicleDetails.getStatus());
            vehicle.setLostDate(vehicleDetails.getLostDate());

            return vehicleRepository.save(vehicle);
        }
        return null;
    }

    public byte[] generatePlateRegistryReportPdf(String startDate, String endDate) {
        List<Vehicle> vehicles;

        // Filter vehicles by date range if provided
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            vehicles = vehicleRepository.findAll().stream()
                    .filter(v -> {
                        if (v.getLostDate() == null || v.getLostDate().isEmpty()) return false;
                        return v.getLostDate().compareTo(startDate) >= 0 &&
                                v.getLostDate().compareTo(endDate) <= 0;
                    })
                    .toList();
        } else {
            vehicles = vehicleRepository.findAll();
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            document.add(new Paragraph("Plate Registry Report"));
            if (startDate != null && endDate != null) {
                document.add(new Paragraph("Date range: " + startDate + " to " + endDate));
            } else {
                document.add(new Paragraph("All Records"));
            }
            document.add(new Paragraph(" "));


            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            table.addCell("Number Plate");
            table.addCell("Owner Name");
            table.addCell("Vehicle Type");
            table.addCell("Status");
            table.addCell("Lost Date");


            for (Vehicle vehicle : vehicles) {
                table.addCell(vehicle.getNumberPlate() != null ? vehicle.getNumberPlate() : "");
                table.addCell(vehicle.getOwnerName() != null ? vehicle.getOwnerName() : "");
                table.addCell(vehicle.getVehicleType() != null ? vehicle.getVehicleType() : "");
                table.addCell(vehicle.getStatus() != null ? vehicle.getStatus() : "");
                table.addCell(vehicle.getLostDate() != null ? vehicle.getLostDate() : "");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating Plate Registry PDF", e);
        }
    }
}
