package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.SafetyLocation;
import com.crimeLink.analyzer.entity.SafetyType;
import com.crimeLink.analyzer.repository.SafetyLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyLocationServiceTest {

    @Mock
    private SafetyLocationRepository repository;

    @InjectMocks
    private SafetyLocationService service;

    @Test
    void getByType_shouldReturnRepositoryData() {
        when(repository.findByType(SafetyType.POLICE)).thenReturn(List.of(new SafetyLocation()));
        assertEquals(1, service.getByType(SafetyType.POLICE).size());
    }

    @Test
    void getAllLocations_shouldReturnRepositoryData() {
        when(repository.findAll()).thenReturn(List.of(new SafetyLocation(), new SafetyLocation()));
        assertEquals(2, service.getAllLocations().size());
    }
}