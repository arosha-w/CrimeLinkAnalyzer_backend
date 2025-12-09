package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.enums.DutyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DutyStatusRequest {
    private DutyStatus status;
}
