package com.hostel.management.repository;

import com.hostel.management.model.Fee;
import com.hostel.management.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudent(Student student);
    List<Fee> findByStatus(Fee.FeeStatus status);
    List<Fee> findByStudentOrderByDueDateDesc(Student student);
}
