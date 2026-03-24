package com.hostel.management.service;

import com.hostel.management.dto.ComplaintRequest;
import com.hostel.management.model.Complaint;
import com.hostel.management.model.Student;
import com.hostel.management.repository.ComplaintRepository;
import com.hostel.management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;

    public ComplaintService(ComplaintRepository complaintRepository, StudentRepository studentRepository) {
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByRaisedAtDesc();
    }

    public List<Complaint> getComplaintsByStatus(Complaint.ComplaintStatus status) {
        return complaintRepository.findByStatusOrderByRaisedAtDesc(status);
    }

    public List<Complaint> getComplaintsByStudent(Long studentId) {
        Student student = findStudent(studentId);
        return complaintRepository.findByStudentOrderByRaisedAtDesc(student);
    }

    @Transactional
    public Complaint createComplaint(ComplaintRequest request) {
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setRaisedAt(LocalDateTime.now());
        complaint.setStatus(Complaint.ComplaintStatus.OPEN);
        if (request.getStudentId() != null) {
            complaint.setStudent(findStudent(request.getStudentId()));
        }
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint updateComplaintStatus(Long id, Complaint.ComplaintStatus status, String resolution) {
        Complaint complaint = findComplaintById(id);
        complaint.setStatus(status);
        if (status == Complaint.ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
            if (resolution != null) complaint.setResolution(resolution);
        }
        return complaintRepository.save(complaint);
    }

    @Transactional
    public void deleteComplaint(Long id) {
        Complaint complaint = findComplaintById(id);
        complaintRepository.delete(complaint);
    }

    private Complaint findComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found: " + id));
    }
}
