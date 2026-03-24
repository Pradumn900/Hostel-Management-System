package com.hostel.management.service;

import com.hostel.management.dto.StudentRequest;
import com.hostel.management.dto.StudentResponse;
import com.hostel.management.model.Room;
import com.hostel.management.model.Student;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
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
