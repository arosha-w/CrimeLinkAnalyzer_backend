package com.crimeLink.analyzer.service;


import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles(){
        return vehicleRepository.findAll();
    }

    public Vehicle addVehicle(Vehicle vehicle){

        if(vehicle.getLostDate() == null || vehicle.getLostDate().isEmpty()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            vehicle.setLostDate(LocalDateTime.now().format(formatter));

        }
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicleById(Long id){
        return vehicleRepository.findById(id).orElse(null);
    }

    public void deleteVehicle(Long id){
        vehicleRepository.deleteById(id);
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails){
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

        if(optionalVehicle.isPresent()){
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
}
