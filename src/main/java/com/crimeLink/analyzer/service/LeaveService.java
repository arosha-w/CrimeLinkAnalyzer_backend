package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.LeaveRequestDTO;
import com.crimeLink.analyzer.dto.LeaveSubmitRequest;
import com.crimeLink.analyzer.dto.LeaveUpdateRequest;
import com.crimeLink.analyzer.entity.LeaveRequest;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.LeaveStatus;
import com.crimeLink.analyzer.repository.LeaveRequestRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    private static final int MAX_MONTHLY_LEAVES = 5;

    @Transactional
    public LeaveRequestDTO submitLeaveRequest(LeaveSubmitRequest request) {

        Integer userId = toUserId(request.getOfficerId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int month = request.getDate().getMonthValue();
        int year = request.getDate().getYear();

        int monthlyCount = leaveRequestRepository.countMonthlyLeaves(userId, year, month);
        if (monthlyCount >= MAX_MONTHLY_LEAVES) {
            throw new RuntimeException("Monthly leave limit exceeded. Maximum "
                    + MAX_MONTHLY_LEAVES + " leaves per month.");
        }

        // Already requested same date (ignore DENIED)
        boolean exists = leaveRequestRepository.existsByUser_UserIdAndDateAndStatusNot(
                userId, request.getDate(), LeaveStatus.DENIED
        );
        if (exists) {
            throw new RuntimeException("Leave already requested for this date");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setUser(user);
        leave.setDate(request.getDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return toDTO(saved);
    }

    public List<LeaveRequestDTO> getOfficerLeaves(Long officerId) {
        Integer userId = toUserId(officerId);

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return leaveRequestRepository.findByUser_UserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getAllLeaveRequests(String month) {

        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthNum = Integer.parseInt(parts[1]);

        LocalDate startDate = LocalDate.of(year, monthNum, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return leaveRequestRepository.findByDateBetween(startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDTO updateLeaveStatus(Long leaveId, LeaveUpdateRequest request, Long respondedByUserId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request already processed");
        }

        LeaveStatus newStatus = LeaveStatus.valueOf(request.getStatus().toUpperCase());

        if (newStatus == LeaveStatus.DENIED &&
                (request.getResponseReason() == null || request.getResponseReason().trim().isEmpty())) {
            throw new RuntimeException("Response reason is required for denial");
        }

        User responder = null;
        if (respondedByUserId != null) {
            Integer responderId = toUserId(respondedByUserId);
            responder = userRepository.findById(responderId)
                    .orElseThrow(() -> new RuntimeException("Responder user not found"));
        }

        leave.setStatus(newStatus);
        leave.setResponseReason(request.getResponseReason());
        leave.setRespondedBy(responder);
        leave.setRespondedDate(LocalDateTime.now());

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return toDTO(updated);
    }

    private Integer toUserId(Long id) {
        if (id == null) throw new RuntimeException("User id is required");
        if (id > Integer.MAX_VALUE) throw new RuntimeException("Invalid user id: " + id);
        return id.intValue();
    }

    private LeaveRequestDTO toDTO(LeaveRequest leave) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leave.getId());

        dto.setOfficerId(leave.getUser() != null ? leave.getUser().getUserId().longValue() : null);
        dto.setOfficerName(leave.getUser() != null ? leave.getUser().getName() : null);

        dto.setDate(leave.getDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus() != null ? leave.getStatus().name() : null);
        dto.setRequestedDate(leave.getRequestedDate());
        dto.setResponseReason(leave.getResponseReason());

        dto.setRespondedBy(
                leave.getRespondedBy() != null ? leave.getRespondedBy().getUserId().longValue() : null
        );
        dto.setRespondedDate(leave.getRespondedDate());

        return dto;
    }
}
