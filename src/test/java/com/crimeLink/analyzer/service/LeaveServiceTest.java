package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.LeaveRequestDTO;
import com.crimeLink.analyzer.dto.LeaveSubmitRequest;
import com.crimeLink.analyzer.dto.LeaveUpdateRequest;
import com.crimeLink.analyzer.entity.LeaveRequest;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.LeaveStatus;
import com.crimeLink.analyzer.repository.LeaveRequestRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    void submitLeaveRequest_shouldThrow_whenUserNotFound() {
        LeaveSubmitRequest request = new LeaveSubmitRequest();
        request.setOfficerId(1L);
        request.setDate(LocalDate.now());
        request.setReason("Medical");

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> leaveService.submitLeaveRequest(request));
    }

    @Test
    void submitLeaveRequest_shouldThrow_whenMonthlyLimitExceeded() {
        User user = new User();
        user.setUserId(1);

        LeaveSubmitRequest request = new LeaveSubmitRequest();
        request.setOfficerId(1L);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setReason("Medical");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(leaveRequestRepository.countMonthlyLeaves(1, 2026, 3)).thenReturn(5);

        assertThrows(RuntimeException.class, () -> leaveService.submitLeaveRequest(request));
    }

    @Test
    void submitLeaveRequest_shouldThrow_whenDuplicateExists() {
        User user = new User();
        user.setUserId(1);

        LeaveSubmitRequest request = new LeaveSubmitRequest();
        request.setOfficerId(1L);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setReason("Medical");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(leaveRequestRepository.countMonthlyLeaves(1, 2026, 3)).thenReturn(2);
        when(leaveRequestRepository.existsByUser_UserIdAndDateAndStatusNot(1, LocalDate.of(2026, 3, 13), LeaveStatus.DENIED))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () -> leaveService.submitLeaveRequest(request));
    }

    @Test
    void submitLeaveRequest_shouldSave_whenValid() {
        User user = new User();
        user.setUserId(1);
        user.setName("Officer A");

        LeaveSubmitRequest request = new LeaveSubmitRequest();
        request.setOfficerId(1L);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setReason("Medical");

        LeaveRequest saved = new LeaveRequest();
        saved.setId(100L);
        saved.setUser(user);
        saved.setDate(request.getDate());
        saved.setReason(request.getReason());
        saved.setStatus(LeaveStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(leaveRequestRepository.countMonthlyLeaves(1, 2026, 3)).thenReturn(1);
        when(leaveRequestRepository.existsByUser_UserIdAndDateAndStatusNot(1, LocalDate.of(2026, 3, 13), LeaveStatus.DENIED))
                .thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(saved);

        LeaveRequestDTO result = leaveService.submitLeaveRequest(request);

        assertEquals(100L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Officer A", result.getOfficerName());
    }

    @Test
    void getOfficerLeaves_shouldReturnLeaveList() {
        User user = new User();
        user.setUserId(1);
        user.setName("Officer A");

        LeaveRequest leave = new LeaveRequest();
        leave.setId(1L);
        leave.setUser(user);
        leave.setDate(LocalDate.now());
        leave.setReason("Medical");
        leave.setStatus(LeaveStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(leaveRequestRepository.findByUser_UserId(1)).thenReturn(List.of(leave));

        List<LeaveRequestDTO> result = leaveService.getOfficerLeaves(1L);

        assertEquals(1, result.size());
        assertEquals("Officer A", result.get(0).getOfficerName());
    }

    @Test
    void getAllLeaveRequests_shouldReturnMonthData() {
        User user = new User();
        user.setUserId(1);
        user.setName("Officer A");

        LeaveRequest leave = new LeaveRequest();
        leave.setId(1L);
        leave.setUser(user);
        leave.setDate(LocalDate.of(2026, 3, 13));
        leave.setReason("Medical");
        leave.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findByDateBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31))
        ).thenReturn(List.of(leave));

        List<LeaveRequestDTO> result = leaveService.getAllLeaveRequests("2026-03");

        assertEquals(1, result.size());
    }

    @Test
    void updateLeaveStatus_shouldThrow_whenLeaveAlreadyProcessed() {
        LeaveRequest leave = new LeaveRequest();
        leave.setId(1L);
        leave.setStatus(LeaveStatus.APPROVED);

        LeaveUpdateRequest request = new LeaveUpdateRequest();
        request.setStatus("DENIED");
        request.setResponseReason("Not enough staff");

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));

        assertThrows(RuntimeException.class, () -> leaveService.updateLeaveStatus(1L, request, 2L));
    }

    @Test
    void updateLeaveStatus_shouldThrow_whenDeniedWithoutReason() {
        LeaveRequest leave = new LeaveRequest();
        leave.setId(1L);
        leave.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequest request = new LeaveUpdateRequest();
        request.setStatus("DENIED");
        request.setResponseReason(" ");

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));

        assertThrows(RuntimeException.class, () -> leaveService.updateLeaveStatus(1L, request, 2L));
    }

    @Test
    void updateLeaveStatus_shouldUpdateSuccessfully() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        User responder = new User();
        responder.setUserId(2);

        LeaveRequest leave = new LeaveRequest();
        leave.setId(1L);
        leave.setUser(officer);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setDate(LocalDate.now());
        leave.setReason("Medical");

        LeaveUpdateRequest request = new LeaveUpdateRequest();
        request.setStatus("APPROVED");
        request.setResponseReason("Approved");

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(userRepository.findById(2)).thenReturn(Optional.of(responder));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDTO result = leaveService.updateLeaveStatus(1L, request, 2L);

        assertEquals("APPROVED", result.getStatus());
        assertEquals(2L, result.getRespondedBy());
    }
}