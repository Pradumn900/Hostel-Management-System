package com.hostel.management.service;

import com.hostel.management.dto.FeeRequest;
import com.hostel.management.model.Fee;
import com.hostel.management.model.Student;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeService(FeeRepository feeRepository, StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    public List<Fee> getAllFees() {
        return feeRepository.findAll();
    }

    public List<Fee> getFeesByStudent(Long studentId) {
        Student student = findStudent(studentId);
        return feeRepository.findByStudentOrderByDueDateDesc(student);
    }

    public List<Fee> getFeesByStatus(Fee.FeeStatus status) {
        return feeRepository.findByStatus(status);
    }

    @Transactional
    public Fee createFee(FeeRequest request) {
        Student student = findStudent(request.getStudentId());
        Fee fee = new Fee();
        applyRequest(fee, student, request);
        return feeRepository.save(fee);
    }

    @Transactional
    public Fee updateFee(Long id, FeeRequest request) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee record not found: " + id));
        Student student = findStudent(request.getStudentId());
        applyRequest(fee, student, request);
        return feeRepository.save(fee);
    }

    @Transactional
    public void deleteFee(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee record not found: " + id));
        feeRepository.delete(fee);
    }

    private void applyRequest(Fee fee, Student student, FeeRequest request) {
        fee.setStudent(student);
        fee.setFeeType(request.getFeeType());
        fee.setAmount(request.getAmount());
        fee.setDueDate(request.getDueDate());
        fee.setPaidDate(request.getPaidDate());
        fee.setStatus(request.getStatus() != null ? request.getStatus() : Fee.FeeStatus.UNPAID);
        fee.setRemarks(request.getRemarks());
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found: " + id));
    }
}
