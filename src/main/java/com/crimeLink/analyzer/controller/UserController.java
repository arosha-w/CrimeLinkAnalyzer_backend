package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponRequestOfficerDTO;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.UserService;
import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/field-officers")
    public List<User> getFieldOfficers() {
        return service.getFieldOfficers();
    }


}

