package com.hostel.management.controller;

import com.hostel.management.dto.AttendanceRequest;
import com.hostel.management.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getAllRoomsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAllRoomsAttendanceSummary(date));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Map<String, Object>>> getRoomAttendance(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByRoom(roomId, date));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(studentId, from, to));
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> saveAttendance(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.saveAttendance(request));
    }
}
