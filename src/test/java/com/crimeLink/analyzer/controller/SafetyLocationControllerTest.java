package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.entity.SafetyLocation;
import com.crimeLink.analyzer.entity.SafetyType;
import com.crimeLink.analyzer.service.SafetyLocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyLocationControllerTest {

    @InjectMocks
    private SafetyLocationController controller;

    @Mock
    private SafetyLocationService service;

    @Test
    void getByType_shouldReturnAll_whenTypeMissing() {
        when(service.getAllLocations()).thenReturn(List.of(new SafetyLocation()));
        assertEquals(1, controller.getByType(null).size());
    }

    @Test
    void getByType_shouldReturnFiltered_whenTypeProvided() {
        when(service.getByType(SafetyType.POLICE)).thenReturn(List.of(new SafetyLocation()));
        assertEquals(1, controller.getByType("police").size());
    }
}