package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyRecommendationRequest;
import com.crimeLink.analyzer.dto.OfficerRecommendationDTO;
import com.crimeLink.analyzer.service.DutyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duty-recommendations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DutyRecommendationController {

    private final DutyRecommendationService recommendationService;

    @PostMapping
    public List<OfficerRecommendationDTO> getRecommendations(
            @RequestBody DutyRecommendationRequest request
    ) {
        return recommendationService.getOfficerRecommendations(request);
    }
}
