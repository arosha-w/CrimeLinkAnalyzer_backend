package com.crimeLink.anayzer.service;

import com.crimeLink.anayzer.entity.User;
import com.crimeLink.anayzer.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getFieldOfficers() {
        return repo.findByRole("FieldOfficer");
    }
}
