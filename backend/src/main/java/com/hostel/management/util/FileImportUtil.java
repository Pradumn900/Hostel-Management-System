package com.hostel.management.util;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FileImportUtil {

    private static final String[] EXPECTED_HEADERS = {
            "registrationNo", "name", "fatherName", "phone", "email", "address", "roomId"
    };

    public static List<Map<String, String>> parseCSV(InputStream inputStream) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headers = reader.readNext();
            
            if (headers == null || headers.length == 0) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            validateHeaders(headers);
            
            String[] line;
            int rowNum = 2; // Start from 2 as row 1 is headers
            while ((line = reader.readNext()) != null) {
                if (line.length == 0) continue; // Skip empty lines
                
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String value = i < line.length ? line[i].trim() : "";
                    row.put(headers[i].trim(), value);
                }
                row.put("_rowNumber", String.valueOf(rowNum));
                result.add(row);
                rowNum++;
            }
        }
        
        return result;
    }

    public static List<Map<String, String>> parseExcel(InputStream inputStream) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Read headers from first row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file is empty");
            }
            
            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = cell != null ? cell.getStringCellValue().trim() : "";
            }
            
            validateHeaders(headers);
            
            // Read data rows
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;
                
                Map<String, String> rowData = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.getCell(i);
                    String value = getCellValueAsString(cell);
                    rowData.put(headers[i], value);
                }
                rowData.put("_rowNumber", String.valueOf(rowNum + 1));
                result.add(rowData);
            }
        }
        
        return result;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static void validateHeaders(String[] headers) throws IllegalArgumentException {
        Set<String> headerSet = new HashSet<>();
        for (String header : headers) {
            headerSet.add(header.toLowerCase());
        }
        
        for (String expected : EXPECTED_HEADERS) {
            if (!headerSet.contains(expected.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Missing required column: " + expected + 
                    ". Expected columns: " + String.join(", ", EXPECTED_HEADERS)
                );
            }
        }
    }

    public static boolean isValidCSV(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    public static boolean isValidExcel(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    public static boolean isValidFile(String filename) {
        return isValidCSV(filename) || isValidExcel(filename);
    }
}
