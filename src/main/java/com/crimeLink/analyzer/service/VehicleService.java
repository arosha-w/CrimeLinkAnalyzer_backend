package com.crimeLink.analyzer.service;


import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles(){
        return vehicleRepository.findAll();
    }

    public Vehicle addVehicle(Vehicle vehicle){

        if(vehicle.getLastUpdate() == null || vehicle.getLastUpdate().isEmpty()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            vehicle.setLastUpdate(LocalDateTime.now().format(formatter));

        }
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicleById(Long id){
        return vehicleRepository.findById(id).orElse(null);
    }

    public void deleteVehicle(Long id){
        vehicleRepository.deleteById(id);
    }
}
