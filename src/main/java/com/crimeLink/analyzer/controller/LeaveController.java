package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.*;
import com.crimeLink.analyzer.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> submitLeaveRequest(
            @Valid @RequestBody LeaveSubmitRequest request) {
        LeaveRequestDTO result = leaveService.submitLeaveRequest(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<LeaveRequestDTO>> getOfficerLeaves(
            @PathVariable Long officerId) {
        List<LeaveRequestDTO> leaves = leaveService.getOfficerLeaves(officerId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaveRequests(
            @RequestParam String month) {
        List<LeaveRequestDTO> leaves = leaveService.getAllLeaveRequests(month);
        return ResponseEntity.ok(leaves);
    }

    @PutMapping("/{leaveId}/status")
    public ResponseEntity<LeaveRequestDTO> updateLeaveStatus(
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long respondedBy = userId != null ? userId : 1L;
        LeaveRequestDTO result = leaveService.updateLeaveStatus(leaveId, request, respondedBy);
        return ResponseEntity.ok(result);
    }
}
