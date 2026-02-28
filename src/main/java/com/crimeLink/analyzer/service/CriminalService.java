package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.Criminal;
import com.crimeLink.analyzer.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for direct criminal record CRUD operations against the database.
 * Handles profile data management (no ML operations).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CriminalService {

    private final CriminalRepository criminalRepository;

    /**
     * Get all criminals as a list of maps (JSON-friendly).
     */
    public List<Map<String, Object>> getAllCriminals() {
        List<Criminal> criminals = criminalRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Criminal c : criminals) {
            result.add(toSummaryMap(c));
        }
        result.sort(Comparator.comparing(m -> (String) m.getOrDefault("name", "")));
        return result;
    }

    /**
     * Get full details for a specific criminal by ID.
     */
    public Optional<Map<String, Object>> getCriminalDetails(String criminalId) {
        return criminalRepository.findById(criminalId).map(this::toDetailMap);
    }

    /**
     * Update an existing criminal's profile fields.
     * Only non-null parameters are applied.
     *
     * @return Updated criminal as a map, or empty if not found.
     */
    public Optional<Map<String, Object>> updateCriminal(
            String criminalId,
            String name, String nic, String riskLevel, String crimeHistory,
            String address, String contactNumber, String secondaryContact,
            String dateOfBirth, String gender, String alias, String status) {

        Optional<Criminal> opt = criminalRepository.findById(criminalId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Criminal c = opt.get();

        if (name != null) c.setName(name);
        if (nic != null) c.setNic(nic);
        if (riskLevel != null) c.setRiskLevel(riskLevel);
        if (crimeHistory != null) c.setCrimeHistory(crimeHistory);
        if (address != null) c.setAddress(address);
        if (contactNumber != null) c.setContactNumber(contactNumber);
        if (secondaryContact != null) c.setSecondaryContact(secondaryContact);
        if (dateOfBirth != null) {
            try {
                c.setDateOfBirth(LocalDate.parse(dateOfBirth));
            } catch (Exception e) {
                log.warn("Invalid date_of_birth format: {}", dateOfBirth);
            }
        }
        if (gender != null) c.setGender(gender);
        if (alias != null) c.setAlias(alias);
        if (status != null) c.setStatus(status);

        Criminal saved = criminalRepository.save(c);
        log.info("Criminal updated: {}", criminalId);

        return Optional.of(toDetailMap(saved));
    }

    /* ─── mapping helpers ─── */

    private Map<String, Object> toSummaryMap(Criminal c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("nic", c.getNic());
        m.put("risk_level", c.getRiskLevel());
        m.put("primary_photo_url", c.getPrimaryPhotoUrl());
        m.put("status", c.getStatus());
        m.put("has_embedding", false); // embedding lives in Python side; list view doesn't need it
        return m;
    }

    private Map<String, Object> toDetailMap(Criminal c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("nic", c.getNic());
        m.put("risk_level", c.getRiskLevel());
        m.put("crime_history", c.getCrimeHistory());
        m.put("primary_photo_url", c.getPrimaryPhotoUrl());
        m.put("status", c.getStatus());
        m.put("address", c.getAddress());
        m.put("contact_number", c.getContactNumber());
        m.put("secondary_contact", c.getSecondaryContact());
        m.put("date_of_birth", c.getDateOfBirth() != null ? c.getDateOfBirth().toString() : null);
        m.put("gender", c.getGender());
        m.put("alias", c.getAlias());
        return m;
    }
}
