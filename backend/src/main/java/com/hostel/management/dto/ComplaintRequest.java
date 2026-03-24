package com.hostel.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintRequest {
    private Long studentId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String resolution;
}
