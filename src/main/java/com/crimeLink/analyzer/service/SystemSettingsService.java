package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.SystemSetting;
import com.crimeLink.analyzer.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final SystemSettingRepository settingRepo;

    // Default values for all supported settings
    private static final Map<String, String> DEFAULTS = Map.of(
            "jwtExpiry", "24",
            "passwordMinLength", "8",
            "maxLoginAttempts", "5",
            "sessionTimeout", "30",
            "backupRetentionDays", "30"
    );

    // Validation rules: key -> [min, max]
    private static final Map<String, int[]> VALIDATION_RULES = Map.of(
            "jwtExpiry", new int[]{1, 720},
            "passwordMinLength", new int[]{6, 128},
            "maxLoginAttempts", new int[]{1, 20},
            "sessionTimeout", new int[]{5, 1440},
            "backupRetentionDays", new int[]{1, 365}
    );

    /**
     * Get all settings, merging stored values with defaults.
     */
    public Map<String, String> getAllSettings() {
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS);

        // Override defaults with DB-stored values
        settingRepo.findAll().forEach(setting ->
                result.put(setting.getSettingKey(), setting.getSettingValue()));

        return result;
    }

    /**
     * Validate and save settings. Only known keys are accepted.
     * Returns the updated map of all settings.
     */
    public Map<String, String> updateSettings(Map<String, String> incoming) {
        for (Map.Entry<String, String> entry : incoming.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Ignore unknown keys
            if (!DEFAULTS.containsKey(key)) {
                log.warn("Ignoring unknown setting key: {}", sanitizeForLog(key));
                continue;
            }

            // Validate numeric value
            int numericValue;
            try {
                numericValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Setting '" + key + "' must be a valid integer, got: " + value);
            }

            int[] range = VALIDATION_RULES.get(key);
            if (range != null && (numericValue < range[0] || numericValue > range[1])) {
                throw new IllegalArgumentException(
                        "Setting '" + key + "' must be between " + range[0] + " and " + range[1] +
                                ", got: " + numericValue);
            }

            // Upsert
            SystemSetting setting = settingRepo.findBySettingKey(key)
                    .orElseGet(() -> {
                        SystemSetting s = new SystemSetting();
                        s.setSettingKey(key);
                        return s;
                    });
            setting.setSettingValue(value);
            settingRepo.save(setting);
        }

        log.info("System settings updated: {}", sanitizeForLog(incoming.keySet().toString()));
        return getAllSettings();
    }

    /**
     * Get a single setting value with fallback to default.
     */
    public String getSetting(String key) {
        return settingRepo.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(DEFAULTS.get(key));
    }

    /**
     * Sanitize input for logging by removing CRLF and other control characters.
     */
    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_");
    }
}
