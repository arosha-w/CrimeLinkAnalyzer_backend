package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController controller;

    @Mock
    private UserService service;

    @Test
    void getAllOfficers_shouldReturnList() {
        when(service.getAllOfficers()).thenReturn(List.of(new User(), new User()));

        List<User> result = controller.getAllOfficers();

        assertEquals(2, result.size());
    }

    @Test
    void getFieldOfficers_shouldReturnList() {
        when(service.getFieldOfficers()).thenReturn(List.of(new User()));

        List<User> result = controller.getFieldOfficers();

        assertEquals(1, result.size());
    }
}