package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.entity.CrimeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrimeLocationDTO {
    private Double latitude;
    private Double longitude;
    private CrimeType crimeType; 
}
