package com.crimeLink.analyzer.service;

import java.time.Instant;
import java.util.List;

import com.crimeLink.analyzer.dto.LocationPointDTO;
import com.crimeLink.analyzer.entity.LocationPoint;

public interface LocationService {
    public void saveBulk(String officerBadgeNo, List<LocationPointDTO> points);

    public List<LocationPoint> getHistory(String officerBadgeNo, Instant from, Instant to);

    public LocationPoint getLastLocation(String officerBadgeNo);
}
