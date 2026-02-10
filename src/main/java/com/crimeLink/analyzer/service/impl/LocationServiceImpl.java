package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.LocationPointDTO;
import com.crimeLink.analyzer.entity.LocationPoint;
import com.crimeLink.analyzer.repository.LocationPointRepository;

import org.springframework.stereotype.Service;

import com.crimeLink.analyzer.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationPointRepository repo;
    private final ObjectMapper mapper;

    @Override
    public void saveBulk(String officerBadgeNo, List<LocationPointDTO> points) {
        var entities = points.stream().filter(p -> p.ts() != null)
                .filter(p -> p.accuracyM() == null || p.accuracyM() <= 50)
                .map(p -> {
                    var e = new LocationPoint();
                    e.setOfficerBadgeNo(officerBadgeNo);
                    e.setTs(p.ts());
                    e.setLatitude(p.latitude());
                    e.setLongitude(p.longitude());
                    e.setAccuracyM(p.accuracyM());
                    e.setSpeedMps(p.speedMps());
                    e.setHeadingDeg(p.headingDeg());
                    e.setProvider(p.provider());

                    try {
                        e.setMeta(p.meta() == null ? null : mapper.valueToTree(p.meta()));
                    } catch (Exception er) {
                        e.setMeta(null);
                    }
                    return e;
                }).toList();
        repo.saveAll(entities);
    }

    @Override
    public List<LocationPoint> getHistory(String officerBadgeNo, Instant from, Instant to) {
        return repo.findByOfficerBadgeNoAndTsBetweenOrderByTsAsc(officerBadgeNo, from, to);
    }

    @Override
    public LocationPoint getLastLocation(String officerBadgeNo) {
        var list = repo.findByOfficerBadgeNoOrderByTsDesc(officerBadgeNo, PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }
}
