package com.hostel.management.controller;

import com.hostel.management.dto.BulkImportResponse;
import com.hostel.management.dto.StudentRequest;
import com.hostel.management.dto.StudentResponse;
import com.hostel.management.service.StudentService;
import com.hostel.management.util.FileImportUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents(@RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(studentService.searchStudents(name));
        }
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<StudentResponse> searchByRegNo(@RequestParam String regNo) {
        return ResponseEntity.ok(studentService.getStudentByRegNo(regNo));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
                                                          @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<BulkImportResponse> bulkImportStudents(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String filename = file.getOriginalFilename();
            if (!FileImportUtil.isValidFile(filename)) {
                throw new IllegalArgumentException("Invalid file type. Supported formats: CSV, XLS, XLSX");
            }

            // Parse file based on type
            List<Map<String, String>> studentData;
            if (FileImportUtil.isValidCSV(filename)) {
                studentData = FileImportUtil.parseCSV(file.getInputStream());
            } else if (FileImportUtil.isValidExcel(filename)) {
                studentData = FileImportUtil.parseExcel(file.getInputStream());
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }

            // Perform bulk import
            BulkImportResponse response = studentService.bulkImportStudents(studentData);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error processing file: " + e.getMessage());
        }
    }
}
