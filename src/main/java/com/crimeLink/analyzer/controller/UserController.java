package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public List<User> getFieldOfficers() {
        return service.getFieldOfficers();
    }

    @GetMapping("/all-officers")
    public List<User> getAllOfficers() {
        return service.getAllOfficers();
    }

}
