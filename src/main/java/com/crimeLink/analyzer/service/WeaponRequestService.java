package com.crimeLink.analyzer.service;

import java.util.List;

import com.crimeLink.analyzer.dto.WeaponRequestDto;

public interface WeaponRequestService {
    WeaponRequestDto createRequest(WeaponRequestDto requestDto);

    List<WeaponRequestDto> getAllRequests();

    List<WeaponRequestDto> getRequestsByUser(Integer userId);

    WeaponRequestDto approvedRequest(Integer requestId);

    WeaponRequestDto rejectedRequest(Integer requestId);
}
