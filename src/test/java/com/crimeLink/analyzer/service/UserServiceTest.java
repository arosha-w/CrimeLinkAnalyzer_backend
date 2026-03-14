package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    @Test
    void getFieldOfficers_shouldReturnFieldOfficerList() {
        when(repo.findByRole("FieldOfficer")).thenReturn(List.of(new User(), new User()));

        List<User> result = service.getFieldOfficers();

        assertEquals(2, result.size());
    }

    @Test
    void getAllOfficers_shouldReturnAllUsers() {
        when(repo.findAll()).thenReturn(List.of(new User()));

        List<User> result = service.getAllOfficers();

        assertEquals(1, result.size());
    }
}