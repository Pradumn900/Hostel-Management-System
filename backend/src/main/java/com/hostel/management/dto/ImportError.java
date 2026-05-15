package com.hostel.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportError {
    private int rowNumber;
    private String registrationNo;
    private String errorMessage;
}
