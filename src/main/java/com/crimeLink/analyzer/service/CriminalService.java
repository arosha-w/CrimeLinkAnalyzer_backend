package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.Criminal;
import com.crimeLink.analyzer.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for direct criminal record CRUD operations against the database.
 * Handles profile data management and coordinates with ML service for embeddings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CriminalService {

    private final CriminalRepository criminalRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final FacialRecognitionService facialRecognitionService;

    /**
     * Create a new criminal record with an optional photo upload.
     *
     * @return A map with criminal_id, name, nic, and message.
     */
    public Map<String, Object> createCriminal(
            String name, String nic, String riskLevel, String crimeHistory,
            String address, String contactNumber, String secondaryContact,
            String dateOfBirth, String gender, String alias, String status,
            MultipartFile photo) {

        // Generate short UUID (matching Python service format)
        String criminalId = UUID.randomUUID().toString().substring(0, 8);

        Criminal c = new Criminal();
        c.setId(criminalId);
        c.setName(name);
        c.setNic(nic);
        c.setRiskLevel(riskLevel != null ? riskLevel : "medium");
        c.setStatus(status != null ? status : "active");
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

        // Upload photo to Supabase Storage if provided
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                photoUrl = supabaseStorageService.uploadPhoto(criminalId, photo);
                c.setPrimaryPhotoUrl(photoUrl);
            } catch (Exception e) {
                log.error("Photo upload failed for criminal {}: {}", criminalId, e.getMessage());
                // Continue without photo — record still gets created
            }
        }

        Criminal saved = criminalRepository.save(c);
        log.info("Criminal created: {} (name: {})", criminalId, name);

        // Generate face embedding via Python ML service (non-blocking)
        boolean hasEmbedding = false;
        if (photo != null && !photo.isEmpty()) {
            try {
                facialRecognitionService.generateEmbedding(criminalId, photo);
                hasEmbedding = true;
                log.info("Face embedding generated for criminal {}", criminalId);
            } catch (Exception e) {
                log.warn("Embedding generation failed for criminal {} (non-fatal): {}", criminalId, e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("criminal_id", saved.getId());
        result.put("name", saved.getName());
        result.put("nic", saved.getNic());
        result.put("photos_stored", photoUrl != null ? 1 : 0);
        result.put("primary_photo_url", photoUrl);
        result.put("has_embedding", hasEmbedding);
        result.put("message", "Criminal registered successfully");
        return result;
    }

    /**
     * Get all criminals as a list of maps (JSON-friendly).
     */
    public List<Map<String, Object>> getAllCriminals() {
        List<Criminal> criminals = criminalRepository.findAll();
        Set<String> idsWithEmbedding = new HashSet<>(criminalRepository.findIdsWithEmbedding());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Criminal c : criminals) {
            result.add(toSummaryMap(c, idsWithEmbedding.contains(c.getId())));
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
            String dateOfBirth, String gender, String alias, String status,
            MultipartFile photo) {

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

        // Handle photo change
        if (photo != null && !photo.isEmpty()) {
            try {
                String newPhotoUrl = supabaseStorageService.uploadPhoto(criminalId, photo);
                c.setPrimaryPhotoUrl(newPhotoUrl);
                log.info("Photo updated for criminal {}", criminalId);
            } catch (Exception e) {
                log.error("Photo upload failed for criminal {}: {}", criminalId, e.getMessage());
            }

            // Regenerate face embedding with new photo
            try {
                facialRecognitionService.generateEmbedding(criminalId, photo);
                log.info("Embedding regenerated for criminal {}", criminalId);
            } catch (Exception e) {
                log.warn("Embedding regeneration failed for criminal {} (non-fatal): {}", criminalId, e.getMessage());
            }
        }

        Criminal saved = criminalRepository.save(c);
        log.info("Criminal updated: {}", criminalId);

        return Optional.of(toDetailMap(saved));
    }

    /**
     * Delete a criminal record, associated storage files, and cascade DB relations.
     *
     * @return true if deleted, false if not found.
     */
    public boolean deleteCriminal(String criminalId) {
        Optional<Criminal> opt = criminalRepository.findById(criminalId);
        if (opt.isEmpty()) {
            return false;
        }

        // Best-effort: delete photos from Supabase Storage
        try {
            supabaseStorageService.deleteFolder(criminalId);
        } catch (Exception e) {
            log.warn("Storage cleanup failed for criminal {} (non-fatal): {}", criminalId, e.getMessage());
        }

        // DB delete — suspect_photos cascade via ON DELETE CASCADE
        criminalRepository.deleteById(criminalId);
        log.info("Criminal deleted: {}", criminalId);
        return true;
    }

    /* ─── mapping helpers ─── */

    private Map<String, Object> toSummaryMap(Criminal c, boolean hasEmbedding) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("nic", c.getNic());
        m.put("risk_level", c.getRiskLevel());
        m.put("primary_photo_url", c.getPrimaryPhotoUrl());
        m.put("status", c.getStatus());
        m.put("has_embedding", hasEmbedding);
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
