package com.hostel.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkImportResponse {
    private int successCount;
    private int totalCount;
    private List<ImportError> errors;
    private String message;

    public BulkImportResponse(int successCount, int totalCount, List<ImportError> errors) {
        this.successCount = successCount;
        this.totalCount = totalCount;
        this.errors = errors;
        this.message = String.format("Successfully imported %d out of %d students", successCount, totalCount);
    }
}
