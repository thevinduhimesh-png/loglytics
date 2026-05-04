package com.ntg.reporting.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

/**
 * DashboardGeneratorService - Writes all months into a SINGLE persistent dashboard file.
 *
 * Flow:
 * 1. Detect month from console filename using MonthDetector
 * 2. Fetch mappings for that month from MappingService
 * 3. Load the existing output dashboard if it exists, otherwise load the template
 * 4. Write this month's values into the correct columns
 * 5. Save back to the single output file (preserving all previously written months)
 */
@Service
public class DashboardGeneratorService {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private MonthDetector monthDetector;

    private static final String DASHBOARD_TEMPLATE = "C:\\Users\\Thevindu.Aditya\\Desktop\\Templates\\NTG 2025 Dashboard_Data Removed.xlsx";
    private static final String OUTPUT_FOLDER      = "C:\\Users\\Thevindu.Aditya\\Desktop\\The end result";
    private static final String OUTPUT_FILE_NAME   = "NTG_2025_Dashboard.xlsx";

    public void generateDashboardFromConsole(String consoleFilePath) throws IOException {
        System.out.println("\n[GENERATOR] ============================================");
        System.out.println("[GENERATOR] Starting dashboard generation...");
        System.out.println("[GENERATOR] Console file: " + consoleFilePath);

        String fileName      = new File(consoleFilePath).getName();
        String detectedMonth = monthDetector.detectMonth(fileName);

        if (detectedMonth == null) {
            System.err.println("[GENERATOR] ❌ ERROR: Could not detect month from filename: " + fileName);
            return;
        }

        System.out.println("[GENERATOR] ✓ Month detected: " + detectedMonth);

        File outputDir = new File(OUTPUT_FOLDER);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(OUTPUT_FOLDER, OUTPUT_FILE_NAME);
        File sourceFile = outputFile.exists() ? outputFile : new File(DASHBOARD_TEMPLATE);

        System.out.println(outputFile.exists()
            ? "[GENERATOR] ✓ Merging " + detectedMonth + " into existing dashboard"
            : "[GENERATOR] ✓ Starting from template");

        try (FileInputStream consoleInputStream   = new FileInputStream(new File(consoleFilePath));
             Workbook consoleWorkbook             = new XSSFWorkbook(consoleInputStream);
             FileInputStream dashboardInputStream = new FileInputStream(sourceFile);
             Workbook dashboardWorkbook           = new XSSFWorkbook(dashboardInputStream)) {

            Sheet dashboardSheet = dashboardWorkbook.getSheet("2025_YTD_A");
            if (dashboardSheet == null) {
                System.err.println("[GENERATOR] ❌ Sheet '2025_YTD_A' not found");
                return;
            }

            var mappingsForMonth = mappingService.getMappingsByMonth(detectedMonth);
            System.out.println("[GENERATOR] ✓ " + mappingsForMonth.size() + " mappings for " + detectedMonth);

            int successCount = 0, errorCount = 0, skipCount = 0;

            for (Object[] mapping : mappingsForMonth) {
                try {
                    String  consoleSheetName = (String)  mapping[0];
                    String  consoleCol       = (String)  mapping[1];
                    Integer consoleRow       = (Integer) mapping[2];
                    Integer dashboardRow     = (Integer) mapping[5];
                    String  dashboardCol     = (String)  mapping[6];

                    if (consoleSheetName == null || consoleCol == null || consoleRow == null
                            || dashboardRow == null || dashboardCol == null) {
                        errorCount++; continue;
                    }

                    Sheet consoleSheet = consoleWorkbook.getSheet(consoleSheetName);
                    if (consoleSheet == null) {
                        skipCount++;
                        if (skipCount <= 5) System.out.println("[GENERATOR] ⚠ Sheet not found: " + consoleSheetName);
                        continue;
                    }

                    double value = excelService.readCellValue(consoleSheet, consoleRow, consoleCol);
                    excelService.writeCellValue(dashboardSheet, dashboardRow, dashboardCol, value);
                    successCount++;

                    if (successCount % 1000 == 0) {
                        System.out.println("[GENERATOR] ... " + successCount + " processed");
                    }

                } catch (Exception e) {
                    errorCount++;
                    if (errorCount <= 5) System.err.println("[GENERATOR] ❌ " + e.getMessage());
                }
            }

            System.out.println("\n[GENERATOR] Summary — ✓ " + successCount + "  ❌ " + errorCount + "  ⚠ " + skipCount);

            File tempFile = new File(OUTPUT_FOLDER, OUTPUT_FILE_NAME.replace(".xlsx", "_TEMP.xlsx"));
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                dashboardWorkbook.write(fos);
            }

            Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("[GENERATOR] ✅ Saved: " + outputFile.getAbsolutePath());
            System.out.println("[GENERATOR] ============================================\n");

        } catch (Exception e) {
            System.err.println("[GENERATOR] ❌ CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Dashboard generation failed", e);
        }
    }

    public void generateDashboardFromConsole(String consoleFilePath, String customOutputPath) throws IOException {
        generateDashboardFromConsole(consoleFilePath);
    }

    public boolean validateConsoleFile(String consoleFilePath) {
        try {
            File file = new File(consoleFilePath);
            if (!file.exists()) { System.err.println("[GENERATOR] File not found: " + consoleFilePath); return false; }
            String month = monthDetector.detectMonth(file.getName());
            if (month == null) { System.err.println("[GENERATOR] Cannot detect month"); return false; }
            var mappings = mappingService.getMappingsByMonth(month);
            if (mappings.isEmpty()) { System.err.println("[GENERATOR] No mappings for: " + month); return false; }
            System.out.println("[GENERATOR] ✓ Valid: " + file.getName() + " (" + month + ")");
            return true;
        } catch (Exception e) {
            System.err.println("[GENERATOR] Validation error: " + e.getMessage());
            return false;
        }
    }
}
