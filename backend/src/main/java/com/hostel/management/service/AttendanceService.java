package com.hostel.management.service;

import com.hostel.management.dto.AttendanceRequest;
import com.hostel.management.model.Attendance;
import com.hostel.management.model.Room;
import com.hostel.management.model.Student;
import com.hostel.management.repository.AttendanceRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             RoomRepository roomRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public List<Map<String, Object>> saveAttendance(AttendanceRequest request) {
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AttendanceRequest.StudentAttendance record : request.getRecords()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found: " + record.getStudentId()));
            Attendance attendance = attendanceRepository
                    .findByStudentAndAttendanceDate(student, date)
                    .orElse(new Attendance());
            attendance.setStudent(student);
            attendance.setAttendanceDate(date);
            attendance.setStatus(record.getStatus());
            attendance.setMarkedAt(LocalDateTime.now());
            attendanceRepository.save(attendance);
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", student.getId());
            entry.put("studentName", student.getName());
            entry.put("status", record.getStatus());
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> getAttendanceByRoom(Long roomId, LocalDate date) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        List<Student> students = studentRepository.findByRoomAndActiveTrue(room);
        if (date == null) date = LocalDate.now();
        final LocalDate finalDate = date;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Student student : students) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", student.getId());
            entry.put("studentName", student.getName());
            entry.put("registrationNo", student.getRegistrationNo());
            Attendance att = attendanceRepository
                    .findByStudentAndAttendanceDate(student, finalDate)
                    .orElse(null);
            entry.put("status", att != null ? att.getStatus().name() : "ABSENT");
            entry.put("markedAt", att != null ? att.getMarkedAt() : null);
            result.add(entry);
        }
        long presentCount = result.stream()
                .filter(e -> "PRESENT".equals(e.get("status"))).count();
        Map<String, Object> summary = new HashMap<>();
        summary.put("roomId", roomId);
        summary.put("roomNumber", room.getRoomNumber());
        summary.put("totalStudents", students.size());
        summary.put("presentCount", presentCount);
        summary.put("date", finalDate.toString());
        summary.put("students", result);
        return List.of(summary);
    }

    public List<Map<String, Object>> getAllRoomsAttendanceSummary(LocalDate date) {
        if (date == null) date = LocalDate.now();
        List<Room> rooms = roomRepository.findAllByOrderByRoomNumberAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : rooms) {
            List<Student> students = studentRepository.findByRoomAndActiveTrue(room);
            if (students.isEmpty()) continue;
            long presentCount = attendanceRepository.countPresentByDateAndRoom(date, room.getId());
            Map<String, Object> entry = new HashMap<>();
            entry.put("roomId", room.getId());
            entry.put("roomNumber", room.getRoomNumber());
            entry.put("floor", room.getFloor());
            entry.put("totalStudents", students.size());
            entry.put("capacity", room.getCapacity());
            entry.put("presentCount", presentCount);
            entry.put("date", date.toString());
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> getStudentAttendance(Long studentId, LocalDate from, LocalDate to) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        List<Attendance> records = attendanceRepository
                .findByStudentAndAttendanceDateBetween(student, from, to);
        return records.stream().map(a -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", a.getAttendanceDate().toString());
            entry.put("status", a.getStatus().name());
            entry.put("markedAt", a.getMarkedAt());
            return entry;
        }).collect(Collectors.toList());
    }
}
