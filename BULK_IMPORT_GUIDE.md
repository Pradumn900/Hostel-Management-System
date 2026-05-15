# Bulk Student Import Feature

## Overview
The Hostel Management System now supports bulk importing students from CSV and Excel files (XLS/XLSX). This feature allows wardens to quickly add multiple students at once instead of entering them individually.

## Supported File Formats

### CSV Format
Create a CSV file with the following columns (in order):
- `registrationNo` - Unique student registration number
- `name` - Full name of the student
- `fatherName` - Father's name
- `phone` - Phone number (10-15 digits, can include +)
- `email` - Valid email address
- `address` - Full residential address
- `roomId` - Room ID for assignment (optional, leave empty for no room assignment)

### Excel Format (XLS/XLSX)
Use the same column structure as CSV but in an Excel workbook. The first row should contain headers.

## File Format Examples

### CSV Example
```csv
registrationNo,name,fatherName,phone,email,address,roomId
2024001,John Doe,Mr. Doe,9876543210,john@example.com,123 Main Street,1
2024002,Jane Smith,Mr. Smith,9876543211,jane@example.com,456 Oak Avenue,2
2024003,Bob Johnson,Mr. Johnson,9876543212,bob@example.com,789 Pine Road,1
2024004,Alice Brown,Mr. Brown,9876543213,alice@example.com,321 Elm Street,
2024005,Charlie Wilson,Mr. Wilson,9876543214,charlie@example.com,654 Maple Drive,2
```

### Excel Format
Create a spreadsheet with:
- Row 1: Headers (registrationNo, name, fatherName, phone, email, address, roomId)
- Row 2+: Student data

## How to Use

### Step 1: Prepare Your File
1. Gather student information
2. Create a CSV or Excel file with the required columns
3. Ensure all required fields are filled in
4. Verify phone numbers and email addresses are valid

### Step 2: Import Students
1. Go to the **Students** page
2. Click the **📥 Import Students** button
3. Click on the file input field to select your CSV/Excel file
4. The system will show a preview of the first 5 rows
5. Review the preview to ensure data looks correct
6. Click **Import Students** button

### Step 3: Review Results
1. The import result modal will show:
   - Number of successfully imported students (Success count)
   - Number of failed imports (Failed count)
   - Detailed error messages for each failed row

## Validation Rules

### Field Validation
- **Registration Number**: Required, must be unique (not already exist in system)
- **Name**: Required, non-empty
- **Father's Name**: Required, non-empty
- **Phone**: Required, 10-15 digits, may include country code (+)
- **Email**: Required, must be valid email format
- **Address**: Required, non-empty
- **Room ID**: Optional, must be valid room ID if provided, cannot exceed room capacity

### Row-Level Validation
- Duplicate registration numbers within the same batch are rejected
- If a room reaches full capacity, students cannot be assigned to it

## Error Handling

If import encounters errors, the system will:
1. Display a detailed error report showing:
   - Row number where error occurred
   - Registration number (if available)
   - Specific error message

2. Common error messages:
   - "Registration number already exists" - Reg no already in system
   - "Invalid phone number format" - Phone doesn't match required format
   - "Invalid email format" - Email doesn't look like valid email
   - "Room X is at full capacity" - Cannot assign to room, it's full
   - "Room not found" - Invalid room ID provided

### Partial Import
- If some rows have errors, valid rows are still imported
- You can see which rows failed and fix them for re-import
- This allows you to update your file and retry

## Tips

1. **Column Order**: Columns don't need to be in a specific order in CSV - header names are used to identify columns
2. **Empty Room ID**: Leave roomId empty or blank if you don't want to assign a student to a room
3. **Batch Import**: No limit on number of students per import
4. **Duplicate Prevention**: System prevents duplicate registration numbers
5. **Room Management**: Before importing with room assignments, ensure all room IDs are valid and have available capacity

## Sample Import Process

1. Start with 10 students to import
2. Upload CSV file
3. System shows 8 successful, 2 failed
4. Review error messages (e.g., invalid phone format, duplicate reg no)
5. Fix the problematic rows in source file
6. Re-import the fixed rows

## Security Notes

- Only authenticated wardens can perform bulk imports
- File is processed on the server side
- All data is validated before insertion to database
- Transactions ensure data consistency (all-or-nothing per row)

## Limitations & Notes

- Maximum file size depends on server configuration (typically 10MB)
- Imported students are marked as active by default
- Room assignments respect room capacity limits
- All data is validated against the same rules as manual entry

## Troubleshooting

### "Invalid file type" error
- Ensure file is CSV, XLS, or XLSX format
- Check file extension

### "Missing required column" error
- Verify all required column headers are present
- Check spelling of column names exactly as shown in examples

### "Phone number format" errors
- Phone should be 10-15 digits
- Can include + for country code
- Remove any special characters like (, ), -, spaces

### "Email format" errors
- Ensure email has @ symbol
- Format should be: username@domain.com
- No spaces in email addresses

### No students imported, all failed
- Check that at least one row of data exists (besides headers)
- Verify all required fields have values
- Check for duplicate registration numbers

## Future Enhancements

Potential improvements for future versions:
- Template download from the system
- Bulk edit/update existing students
- Import schedule for automated imports
- Import history and logs
- Rollback capability for imports
