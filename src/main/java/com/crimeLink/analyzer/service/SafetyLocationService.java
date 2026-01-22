package com.crimeLink.analyzer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crimeLink.analyzer.entity.SafetyLocation;
import com.crimeLink.analyzer.entity.SafetyType;
import com.crimeLink.analyzer.repository.SafetyLocationRepository;

@Service
public class SafetyLocationService {

    @Autowired
    private SafetyLocationRepository repository;

    public List<SafetyLocation> getByType(SafetyType type) {
        return repository.findByType(type);
    }

    public List<SafetyLocation> getAllLocations() {
        return repository.findAll();
    }
}
