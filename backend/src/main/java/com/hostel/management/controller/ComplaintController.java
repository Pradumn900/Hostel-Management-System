package com.hostel.management.controller;

import com.hostel.management.dto.ComplaintRequest;
import com.hostel.management.model.Complaint;
import com.hostel.management.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            try {
                Complaint.ComplaintStatus cs = Complaint.ComplaintStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(complaintService.getComplaintsByStatus(cs));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(complaintService.getAllComplaints());
            }
        }
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Complaint>> getComplaintsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(complaintService.getComplaintsByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.createComplaint(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Complaint> updateStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        Complaint.ComplaintStatus status = Complaint.ComplaintStatus.valueOf(body.get("status").toUpperCase());
        String resolution = body.get("resolution");
        return ResponseEntity.ok(complaintService.updateComplaintStatus(id, status, resolution));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.noContent().build();
    }
}
