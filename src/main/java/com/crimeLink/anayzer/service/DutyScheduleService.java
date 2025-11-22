package com.crimeLink.anayzer.service;

import com.crimeLink.anayzer.dto.DutyScheduleRequest;
import com.crimeLink.anayzer.entity.DutySchedule;
import com.crimeLink.anayzer.repository.DutyScheduleRepository;
import org.springframework.stereotype.Service;

@Service
public class DutyScheduleService {

    private final DutyScheduleRepository repo;

    public DutyScheduleService(DutyScheduleRepository repo) {
        this.repo = repo;
    }

    public DutySchedule saveSchedule(DutyScheduleRequest req) {

        DutySchedule d = new DutySchedule();

        d.setDatetime(req.getDatetime());
        d.setDuration(req.getDuration());
        d.setTaskType(req.getTaskType());
        d.setStatus(req.getStatus());
        d.setAssignedOfficer(req.getAssignedOfficer());
        d.setLocation(req.getLocation());
        d.setDescription(req.getDescription());

        return repo.save(d);
    }
}
