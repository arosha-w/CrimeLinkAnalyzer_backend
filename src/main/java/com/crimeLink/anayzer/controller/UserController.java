package com.crimeLink.anayzer.controller;

import com.crimeLink.anayzer.entity.User;
import com.crimeLink.anayzer.service.UserService;
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

