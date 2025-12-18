package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:5173")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getVehicles(){
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestBody Vehicle vehicle){
        Vehicle savedVehicle = vehicleService.addVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
    }

    @GetMapping("/{id}")  // ← Path variable: /api/vehicles/5
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        // ↑ @PathVariable extracts "5" from URL
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return vehicle != null
                ? ResponseEntity.ok(vehicle)  // ← 200 OK
                : ResponseEntity.notFound().build();  // ← 404 Not Found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();  // ← 204 No Content
    }
}

