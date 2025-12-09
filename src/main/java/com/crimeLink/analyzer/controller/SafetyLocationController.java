package com.crimeLink.analyzer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crimeLink.analyzer.entity.SafetyLocation;
import com.crimeLink.analyzer.entity.SafetyType;
import com.crimeLink.analyzer.service.SafetyLocationService;

@RestController
@RequestMapping("/api/safety-locations")
@CrossOrigin
public class SafetyLocationController {

    @Autowired
    private SafetyLocationService service;

    @GetMapping
    public List<SafetyLocation> getByType(@RequestParam(required = false) String type) {
        if (type == null || type.isEmpty()) {
            return service.getAllLocations();
        }

        SafetyType safetyType = SafetyType.valueOf(type.toUpperCase());
        return service.getByType(safetyType);
    }
}
