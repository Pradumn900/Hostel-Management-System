package com.hostel.management.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StudentRequest {
    @NotBlank(message = "Registration number is required")
    private String registrationNo;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Father's name is required")
    private String fatherName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    private Long roomId;
}
