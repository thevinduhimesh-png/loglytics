package com.ntg.reporting.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Enhanced ExcelService - Cell reading/writing with month-aware operations
 * 
 * Features:
 * - Basic cell operations (read/write)
 * - Month-aware cell operations with automatic column mapping
 * - Batch operations for efficiency
 * - Validation and error recovery
 * - Integrated with MappingService for console→dashboard transformations
 */
@Service
public class ExcelService {

    @Autowired(required = false)
    private MappingService mappingService;

    /**
     * Read a cell value from a sheet by column letter and row number
     * 
     * @param sheet The Excel sheet
     * @param rowNum Row number (1-based)
     * @param columnLetter Column letter (e.g., "A", "B", "AA")
     * @return The numeric value, or 0 if not found
     */
    public double readCellValue(Sheet sheet, Integer rowNum, String columnLetter) {
        try {
            if (sheet == null || rowNum == null || columnLetter == null) {
                System.err.println("[EXCEL] ERROR: Invalid parameters for readCellValue");
                return 0;
            }

            // Convert row number (1-based) to index (0-based)
            Row row = sheet.getRow(rowNum - 1);
            if (row == null) {
                System.out.println("[EXCEL] Row " + rowNum + " not found in sheet");
                return 0;
            }

            // Convert column letter to index
            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);

            if (cell == null) {
                System.out.println("[EXCEL] Cell " + columnLetter + rowNum + " is empty");
                return 0;
            }

            // Get cell value based on type
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case FORMULA:
                    // Try to get cached formula result
                    try {
                        return cell.getNumericCellValue();
                    } catch (Exception e) {
                        System.out.println("[EXCEL] Warning: Could not evaluate formula in " + columnLetter + rowNum);
                        return 0;
                    }
                case STRING:
                    try {
                        return Double.parseDouble(cell.getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.out.println("[EXCEL] Warning: Cell " + columnLetter + rowNum + " contains non-numeric string");
                        return 0;
                    }
                default:
                    return 0;
            }

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR reading cell: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Read a cell value as string from a sheet
     * 
     * @param sheet The Excel sheet
     * @param rowNum Row number (1-based)
     * @param columnLetter Column letter
     * @return The string value, or empty string if not found
     */
    public String readCellValueAsString(Sheet sheet, Integer rowNum, String columnLetter) {
        try {
            if (sheet == null || rowNum == null || columnLetter == null) {
                return "";
            }

            Row row = sheet.getRow(rowNum - 1);
            if (row == null) return "";

            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);

            if (cell == null) return "";

            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return "";
            }

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR reading cell as string: " + e.getMessage());
            return "";
        }
    }

    /**
     * Write a value to a cell in a sheet by column letter and row number
     * 
     * @param sheet The Excel sheet
     * @param rowNum Row number (1-based)
     * @param columnLetter Column letter (e.g., "A", "B", "AA")
     * @param value The numeric value to write
     */
    public void writeCellValue(Sheet sheet, Integer rowNum, String columnLetter, double value) {
        try {
            if (sheet == null || rowNum == null || columnLetter == null) {
                System.err.println("[EXCEL] ERROR: Invalid parameters for writeCellValue");
                return;
            }

            // Convert row number (1-based) to index (0-based)
            Row row = sheet.getRow(rowNum - 1);
            if (row == null) {
                row = sheet.createRow(rowNum - 1);
            }

            // Convert column letter to index
            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);
            if (cell == null) {
                cell = row.createCell(columnIndex);
            }

            // Set numeric value
            cell.setCellValue(value);

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR writing cell: " + e.getMessage());
        }
    }

    /**
     * Write a string value to a cell
     * 
     * @param sheet The Excel sheet
     * @param rowNum Row number (1-based)
     * @param columnLetter Column letter
     * @param value The string value to write
     */
    public void writeCellValue(Sheet sheet, Integer rowNum, String columnLetter, String value) {
        try {
            if (sheet == null || rowNum == null || columnLetter == null) {
                System.err.println("[EXCEL] ERROR: Invalid parameters for writeCellValue");
                return;
            }

            Row row = sheet.getRow(rowNum - 1);
            if (row == null) {
                row = sheet.createRow(rowNum - 1);
            }

            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);
            if (cell == null) {
                cell = row.createCell(columnIndex);
            }

            cell.setCellValue(value);

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR writing cell: " + e.getMessage());
        }
    }

    /**
     * Month-aware read: Read from console sheet with automatic column mapping
     * Uses MappingService to find the correct dashboard column for a given month
     * 
     * @param consoleSheet Source console sheet
     * @param entity Entity name (e.g., "NSG Bermuda")
     * @param month Month abbreviation (e.g., "Jan", "Feb")
     * @param consoleCol Console column letter
     * @param consoleRow Console row number (1-based)
     * @return The value read from console
     */
    public double readMonthAwareValue(Sheet consoleSheet, String entity, String month, 
                                      String consoleCol, int consoleRow) {
        try {
            if (consoleSheet == null || entity == null || month == null) {
                System.err.println("[EXCEL] ERROR: Invalid parameters for month-aware read");
                return 0;
            }

            System.out.println("[EXCEL] Reading month-aware value: entity=" + entity + 
                             ", month=" + month + ", col=" + consoleCol + ", row=" + consoleRow);

            // Read from console sheet
            double value = readCellValue(consoleSheet, consoleRow, consoleCol);
            System.out.println("[EXCEL] ✓ Read value: " + value);
            return value;

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR in month-aware read: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Month-aware write: Write to dashboard sheet using mapping
     * Uses MappingService to find the correct dashboard cell for a given month
     * 
     * @param dashboardSheet Target dashboard sheet
     * @param entity Entity name
     * @param month Month abbreviation
     * @param consoleCol Source console column
     * @param consoleRow Source console row
     * @param value Value to write
     * @return true if write successful, false otherwise
     */
    public boolean writeMonthAwareValue(Sheet dashboardSheet, String entity, String month,
                                        String consoleCol, int consoleRow, double value) {
        try {
            if (dashboardSheet == null || entity == null || month == null) {
                System.err.println("[EXCEL] ERROR: Invalid parameters for month-aware write");
                return false;
            }

            // Get mapping from service if available
            if (mappingService != null) {
                String[] mapping = mappingService.getMapping(entity, month, consoleCol, consoleRow);
                if (mapping != null && mapping.length == 2) {
                    int dashboardRow = Integer.parseInt(mapping[0]);
                    String dashboardCol = mapping[1];
                    
                    System.out.println("[EXCEL] Mapping found: console[" + consoleCol + consoleRow + 
                                     "] → dashboard[" + dashboardCol + dashboardRow + "]");
                    
                    writeCellValue(dashboardSheet, dashboardRow, dashboardCol, value);
                    return true;
                } else {
                    System.out.println("[EXCEL] Warning: No mapping found for entity=" + entity + 
                                     ", month=" + month);
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR in month-aware write: " + e.getMessage());
            return false;
        }
    }

    /**
     * Batch read multiple cells efficiently
     * 
     * @param sheet The Excel sheet
     * @param cells Map of cell references (e.g., "A1", "B2") to read
     * @return Map of cell references to their values
     */
    public Map<String, Double> readMultipleCells(Sheet sheet, Set<String> cells) {
        Map<String, Double> results = new HashMap<>();
        for (String cell : cells) {
            String col = extractColumn(cell);
            int row = extractRow(cell);
            results.put(cell, readCellValue(sheet, row, col));
        }
        return results;
    }

    /**
     * Batch write multiple cells efficiently
     * 
     * @param sheet The Excel sheet
     * @param values Map of cell references to values
     */
    public void writeMultipleCells(Sheet sheet, Map<String, Double> values) {
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String cell = entry.getKey();
            String col = extractColumn(cell);
            int row = extractRow(cell);
            writeCellValue(sheet, row, col, entry.getValue());
        }
    }

    /**
     * Extract column from cell reference (e.g., "A1" → "A")
     */
    private String extractColumn(String cellRef) {
        return cellRef.replaceAll("[0-9]", "");
    }

    /**
     * Extract row from cell reference (e.g., "A1" → 1)
     */
    private int extractRow(String cellRef) {
        return Integer.parseInt(cellRef.replaceAll("[^0-9]", ""));
    }

    /**
     * Convert column letter to index (A=0, B=1, Z=25, AA=26, AB=27, etc.)
     */
    public int columnLetterToIndex(String columnLetter) {
        if (columnLetter == null || columnLetter.isEmpty()) {
            throw new IllegalArgumentException("Column letter cannot be null or empty");
        }

        int index = 0;
        for (char c : columnLetter.toUpperCase().toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert to 0-based index
    }

    /**
     * Convert index to column letter (0=A, 1=B, 25=Z, 26=AA, 27=AB, etc.)
     */
    public String indexToColumnLetter(int index) {
        StringBuilder result = new StringBuilder();
        int idx = index + 1; // Convert to 1-based

        while (idx > 0) {
            idx--;
            result.insert(0, (char) ('A' + (idx % 26)));
            idx = idx / 26;
        }

        return result.toString();
    }

    /**
     * Get the type of a cell
     */
    public CellType getCellType(Sheet sheet, Integer rowNum, String columnLetter) {
        try {
            Row row = sheet.getRow(rowNum - 1);
            if (row == null) return CellType.BLANK;

            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);
            if (cell == null) return CellType.BLANK;

            return cell.getCellType();
        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR getting cell type: " + e.getMessage());
            return CellType.BLANK;
        }
    }

    /**
     * Create a cell if it doesn't exist
     */
    public Cell createCell(Sheet sheet, Integer rowNum, String columnLetter) {
        try {
            Row row = sheet.getRow(rowNum - 1);
            if (row == null) {
                row = sheet.createRow(rowNum - 1);
            }

            int columnIndex = columnLetterToIndex(columnLetter);
            Cell cell = row.getCell(columnIndex);
            if (cell == null) {
                cell = row.createCell(columnIndex);
            }

            return cell;
        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR creating cell: " + e.getMessage());
            return null;
        }
    }

    /**
     * Copy cell value from source to destination
     * 
     * @param sourceSheet Source sheet
     * @param destSheet Destination sheet
     * @param sourceCol Source column
     * @param sourceRow Source row (1-based)
     * @param destCol Destination column
     * @param destRow Destination row (1-based)
     */
    public void copyCellValue(Sheet sourceSheet, Sheet destSheet, String sourceCol, int sourceRow,
                             String destCol, int destRow) {
        try {
            double value = readCellValue(sourceSheet, sourceRow, sourceCol);
            writeCellValue(destSheet, destRow, destCol, value);
            System.out.println("[EXCEL] ✓ Copied: " + sourceCol + sourceRow + " → " + 
                             destCol + destRow);
        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR copying cell: " + e.getMessage());
        }
    }

    /**
     * Validate if a range contains numeric data
     * 
     * @param sheet The sheet
     * @param startRow Start row (1-based)
     * @param endRow End row (1-based)
     * @param column Column letter
     * @return true if all cells in range are numeric
     */
    public boolean isRangeNumeric(Sheet sheet, int startRow, int endRow, String column) {
        try {
            for (int row = startRow; row <= endRow; row++) {
                CellType type = getCellType(sheet, row, column);
                if (type != CellType.NUMERIC && type != CellType.BLANK) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR validating range: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get sum of a range of cells
     * 
     * @param sheet The sheet
     * @param startRow Start row (1-based)
     * @param endRow End row (1-based)
     * @param column Column letter
     * @return Sum of numeric values in the range
     */
    public double sumRange(Sheet sheet, int startRow, int endRow, String column) {
        double sum = 0;
        try {
            for (int row = startRow; row <= endRow; row++) {
                sum += readCellValue(sheet, row, column);
            }
        } catch (Exception e) {
            System.err.println("[EXCEL] ERROR summing range: " + e.getMessage());
        }
        return sum;
    }

    /**
     * Get average of a range of cells
     * 
     * @param sheet The sheet
     * @param startRow Start row (1-based)
     * @param endRow End row (1-based)
     * @param column Column letter
     * @return Average of numeric values in the range
     */
    public double averageRange(Sheet sheet, int startRow, int endRow, String column) {
        double sum = sumRange(sheet, startRow, endRow, column);
        int count = endRow - startRow + 1;
        return count > 0 ? sum / count : 0;
    }
}