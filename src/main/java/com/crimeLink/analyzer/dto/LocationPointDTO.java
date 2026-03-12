package com.crimeLink.analyzer.dto;

import java.time.Instant;
import java.util.Map;

public record LocationPointDTO(
        Instant ts,
        double latitude,
        double longitude,
        Float accuracyM,
        Float speedMps,
        Float headingDeg,
        String provider,
        Map<String, Object> meta) {

}
