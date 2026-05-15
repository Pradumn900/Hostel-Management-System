package com.hostel.management.service;

import com.hostel.management.dto.BulkImportResponse;
import com.hostel.management.dto.ImportError;
import com.hostel.management.dto.StudentRequest;
import com.hostel.management.dto.StudentResponse;
import com.hostel.management.model.Room;
import com.hostel.management.model.Student;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    public StudentService(StudentRepository studentRepository,
                          RoomRepository roomRepository,
                          RoomService roomService) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findByActiveTrue().stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }

    public StudentResponse getStudentById(Long id) {
        Student student = findStudentById(id);
        return StudentResponse.fromStudent(student);
    }

    public StudentResponse getStudentByRegNo(String regNo) {
        Student student = studentRepository.findByRegistrationNo(regNo)
                .orElseThrow(() -> new RuntimeException("Student not found with registration no: " + regNo));
        return StudentResponse.fromStudent(student);
    }

    public List<StudentResponse> searchStudents(String name) {
        return studentRepository.findByNameContainingIgnoreCaseAndActiveTrue(name).stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Registration number already exists: " + request.getRegistrationNo());
        }
        Student student = new Student();
        applyRequest(student, request);
        student = studentRepository.save(student);
        if (student.getRoom() != null) {
            roomService.updateOccupancy(student.getRoom().getId());
        }
        return StudentResponse.fromStudent(student);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudentById(id);
        Long oldRoomId = student.getRoom() != null ? student.getRoom().getId() : null;
        if (!student.getRegistrationNo().equals(request.getRegistrationNo()) &&
                studentRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Registration number already exists: " + request.getRegistrationNo());
        }
        applyRequest(student, request);
        student = studentRepository.save(student);
        // Update occupancy for old and new room
        if (oldRoomId != null) roomService.updateOccupancy(oldRoomId);
        if (student.getRoom() != null) roomService.updateOccupancy(student.getRoom().getId());
        return StudentResponse.fromStudent(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudentById(id);
        Long roomId = student.getRoom() != null ? student.getRoom().getId() : null;
        student.setActive(false);
        student.setRoom(null);
        studentRepository.save(student);
        if (roomId != null) roomService.updateOccupancy(roomId);
    }

    @Transactional
    public BulkImportResponse bulkImportStudents(List<Map<String, String>> studentData) {
        List<ImportError> errors = new ArrayList<>();
        int successCount = 0;

        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Pattern phonePattern = Pattern.compile("^[+]?[0-9]{10,15}$");

        Set<String> processedRegNumbers = new HashSet<>();

        for (Map<String, String> row : studentData) {
            String rowNumber = row.getOrDefault("_rowNumber", "unknown");
            int rowNum = Integer.parseInt(rowNumber);
            
            try {
                // Extract and validate data
                String registrationNo = row.getOrDefault("registrationNo", "").trim();
                String name = row.getOrDefault("name", "").trim();
                String fatherName = row.getOrDefault("fatherName", "").trim();
                String phone = row.getOrDefault("phone", "").trim();
                String email = row.getOrDefault("email", "").trim();
                String address = row.getOrDefault("address", "").trim();
                String roomIdStr = row.getOrDefault("roomId", "").trim();

                // Validation
                StringBuilder validationError = new StringBuilder();

                if (registrationNo.isEmpty()) {
                    validationError.append("Registration number is required. ");
                } else if (studentRepository.existsByRegistrationNo(registrationNo)) {
                    validationError.append("Registration number already exists. ");
                } else if (processedRegNumbers.contains(registrationNo)) {
                    validationError.append("Duplicate registration number in this batch. ");
                }

                if (name.isEmpty()) {
                    validationError.append("Name is required. ");
                }
                if (fatherName.isEmpty()) {
                    validationError.append("Father's name is required. ");
                }
                if (phone.isEmpty()) {
                    validationError.append("Phone number is required. ");
                } else if (!phonePattern.matcher(phone).matches()) {
                    validationError.append("Invalid phone number format. ");
                }
                if (email.isEmpty()) {
                    validationError.append("Email is required. ");
                } else if (!emailPattern.matcher(email).matches()) {
                    validationError.append("Invalid email format. ");
                }
                if (address.isEmpty()) {
                    validationError.append("Address is required. ");
                }

                if (validationError.length() > 0) {
                    errors.add(new ImportError(rowNum, registrationNo, validationError.toString().trim()));
                    continue;
                }

                // Create StudentRequest
                StudentRequest request = new StudentRequest();
                request.setRegistrationNo(registrationNo);
                request.setName(name);
                request.setFatherName(fatherName);
                request.setPhone(phone);
                request.setEmail(email);
                request.setAddress(address);

                // Handle room assignment
                if (!roomIdStr.isEmpty()) {
                    try {
                        Long roomId = Long.parseLong(roomIdStr);
                        Room room = roomRepository.findById(roomId)
                                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
                        
                        // Check room capacity
                        long activeStudents = studentRepository.findByRoomAndActiveTrue(room).size();
                        if (activeStudents >= room.getCapacity()) {
                            errors.add(new ImportError(rowNum, registrationNo, 
                                "Room " + room.getRoomNumber() + " is at full capacity"));
                            continue;
                        }
                        request.setRoomId(roomId);
                    } catch (NumberFormatException e) {
                        errors.add(new ImportError(rowNum, registrationNo, "Invalid room ID format"));
                        continue;
                    }
                }

                // Create student
                Student student = new Student();
                applyRequest(student, request);
                studentRepository.save(student);
                
                if (student.getRoom() != null) {
                    roomService.updateOccupancy(student.getRoom().getId());
                }

                processedRegNumbers.add(registrationNo);
                successCount++;

            } catch (Exception e) {
                String regNo = row.getOrDefault("registrationNo", "unknown");
                errors.add(new ImportError(rowNum, regNo, e.getMessage()));
            }
        }

        return new BulkImportResponse(successCount, studentData.size(), errors);
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setRegistrationNo(request.getRegistrationNo());
        student.setName(request.getName());
        student.setFatherName(request.getFatherName());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found: " + request.getRoomId()));
            // Check room capacity (only if room is changing)
            if (student.getRoom() == null || !student.getRoom().getId().equals(room.getId())) {
                long activeStudents = studentRepository.findByRoomAndActiveTrue(room).size();
                if (activeStudents >= room.getCapacity()) {
                    throw new RuntimeException("Room " + room.getRoomNumber() + " is at full capacity");
                }
            }
            student.setRoom(room);
        } else {
            student.setRoom(null);
        }
    }

    public Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }
}
