package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.UserRepository;
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
