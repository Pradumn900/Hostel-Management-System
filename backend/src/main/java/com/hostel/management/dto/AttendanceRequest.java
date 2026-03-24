package com.hostel.management.dto;

import com.hostel.management.model.Attendance;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequest {
    private LocalDate date;
    private List<StudentAttendance> records;

    @Data
    public static class StudentAttendance {
        private Long studentId;
        private Attendance.AttendanceStatus status;
    }
}
