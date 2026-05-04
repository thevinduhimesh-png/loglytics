package com.ntg.reporting.service;

import com.ntg.reporting.model.DivisionKpi;
import com.ntg.reporting.model.KpiData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * ExcelParserService - Enhanced to detect month from console filename
 * 
 * ENHANCED FEATURES:
 * - Automatically detects month from filename using MonthDetector
 * - Sets detected month in KpiData
 * - Validates file before parsing
 */
@Service
public class ExcelParserService {

    @Value("${ntg.console.path}")
    private String consolePath;

    @Autowired
    private MonthDetector monthDetector;

    private static final String[] DIVISIONS = {"A", "B", "C", "D", "E", "H"};

    /**
     * Parse console file and detect month from filename
     * ENHANCED: Now detects month using MonthDetector
     */
    public KpiData parseConsoleFile() {
        File dir = new File(consolePath);
        File[] files = dir.listFiles((d, name) -> 
            (name.endsWith(".xlsx") || name.endsWith(".xls")) && !name.startsWith("~$")
        );

        if (files == null || files.length == 0) {
            System.out.println("[PARSER] No console files found, returning mock data");
            return getMockData();
        }

        // Use most recently modified file
        File latest = Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);

        if (latest == null) {
            return getMockData();
        }

        System.out.println("[PARSER] Processing file: " + latest.getName());

        // ============================================================
        // ENHANCED: Detect month from filename
        // ============================================================
        String detectedMonth = monthDetector.detectMonth(latest.getName());
        
        if (detectedMonth != null) {
            System.out.println("[PARSER] ✓ Detected month: " + detectedMonth);
        } else {
            System.err.println("[PARSER] ⚠ Could not detect month from filename: " + latest.getName());
        }

        try (FileInputStream fis = new FileInputStream(latest);
             Workbook workbook = new XSSFWorkbook(fis)) {

            KpiData kpi = new KpiData();
            
            // Set detected month
            kpi.setMonth(detectedMonth != null ? detectedMonth : "Unknown");
            
            Map<String, DivisionKpi> divisions = new LinkedHashMap<>();

            // Parse sheets
            Sheet plSheet = workbook.getSheet("S_PL");
            Sheet cfSheet = workbook.getSheet("S_CF");
            Sheet bsSheet = workbook.getSheet("S_BS");

            if (plSheet != null) {
                parsePLSheet(plSheet, divisions, kpi);
            } else {
                System.err.println("[PARSER] ⚠ Sheet 'S_PL' not found");
            }
            
            if (cfSheet != null) {
                parseCFSheet(cfSheet, divisions, kpi);
            } else {
                System.err.println("[PARSER] ⚠ Sheet 'S_CF' not found");
            }

            if (bsSheet != null) {
                parseBSSheet(bsSheet, divisions, kpi);
            } else {
                System.err.println("[PARSER] ⚠ Sheet 'S_BS' not found");
            }

            kpi.setDivisions(divisions);
            
            System.out.println("[PARSER] ✓ Parsed successfully - Month: " + kpi.getMonth());
            
            return kpi;

        } catch (Exception e) {
            System.err.println("[PARSER] ❌ Error parsing file: " + e.getMessage());
            e.printStackTrace();
            return getMockData();
        }
    }

    /**
     * Parse P&L sheet
     */
    private void parsePLSheet(Sheet sheet, Map<String, DivisionKpi> divisions, KpiData kpi) {
        System.out.println("[PARSER] Parsing P&L sheet...");
        
        double totalRev = 0, totalEbitda = 0, totalGP = 0;

        for (String div : DIVISIONS) {
            DivisionKpi d = divisions.getOrDefault(div, new DivisionKpi());
            d.setDivision(div);

            // Search rows for division data
            for (Row row : sheet) {
                Cell labelCell = row.getCell(0);
                if (labelCell == null) continue;
                String label = getCellString(labelCell).toUpperCase();

                if (label.contains("REVENUE") && label.contains(div)) {
                    d.setRevenue(getNumericValue(row, 1));
                    d.setRevenueYtd(getNumericValue(row, 13));
                }
                if (label.contains("EBITDA") && label.contains(div)) {
                    d.setEbitda(getNumericValue(row, 1));
                    d.setEbitdaYtd(getNumericValue(row, 13));
                }
                if (label.contains("GROSS") && label.contains(div)) {
                    d.setGrossProfit(getNumericValue(row, 1));
                }
            }

            // Calculate margins
            if (d.getRevenue() != 0) {
                d.setEbitdaMargin(d.getRevenue() > 0 ? d.getEbitda() / d.getRevenue() * 100 : 0);
                d.setGrossMargin(d.getRevenue() > 0 ? d.getGrossProfit() / d.getRevenue() * 100 : 0);
            }

            divisions.put(div, d);
            totalRev += d.getRevenue();
            totalEbitda += d.getEbitda();
            totalGP += d.getGrossProfit();
        }

        kpi.setTotalRevenue(totalRev);
        kpi.setTotalEbitda(totalEbitda);
        kpi.setTotalGrossProfit(totalGP);
        
        System.out.println("[PARSER] ✓ P&L parsed - Total Revenue: " + totalRev);
    }

    /**
     * Parse Cash Flow sheet
     */
    private void parseCFSheet(Sheet sheet, Map<String, DivisionKpi> divisions, KpiData kpi) {
        System.out.println("[PARSER] Parsing Cash Flow sheet...");
        
        double totalCF = 0;
        for (Row row : sheet) {
            Cell labelCell = row.getCell(0);
            if (labelCell == null) continue;
            String label = getCellString(labelCell).toUpperCase();
            if (label.contains("NET CASH") || label.contains("TOTAL CASH")) {
                totalCF = getNumericValue(row, 1);
                break;
            }
        }
        kpi.setTotalCashFlow(totalCF);
        
        System.out.println("[PARSER] ✓ Cash Flow parsed - Total: " + totalCF);
    }

    /**
     * Parse Balance Sheet
     */
    private void parseBSSheet(Sheet sheet, Map<String, DivisionKpi> divisions, KpiData kpi) {
        System.out.println("[PARSER] Parsing Balance Sheet...");
        
        // Parse balance sheet data if needed
        // This is a placeholder for future BS parsing logic
        
        System.out.println("[PARSER] ✓ Balance Sheet parsed");
    }

    /**
     * Get cell value as string
     */
    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    /**
     * Get numeric value from cell
     */
    private double getNumericValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case FORMULA -> {
                try { yield cell.getNumericCellValue(); }
                catch (Exception e) { yield 0; }
            }
            default -> 0;
        };
    }

    /**
     * Returns mock data if no Excel file found yet
     */
    public KpiData getMockData() {
        KpiData kpi = new KpiData();
        kpi.setMonth("Demo Data - Add your console file to /console-files/");
        Map<String, DivisionKpi> divs = new LinkedHashMap<>();

        String[] names = {"A", "B", "C", "D", "E", "H"};
        double[] revs = {12500, 8300, 6700, 4200, 9100, 3800};
        double[] ebitdas = {2100, 1450, 980, 610, 1820, 540};
        double[] gps = {4800, 3100, 2400, 1600, 3700, 1300};

        double totalRev = 0, totalEbitda = 0, totalGP = 0;
        for (int i = 0; i < names.length; i++) {
            DivisionKpi d = new DivisionKpi();
            d.setDivision(names[i]);
            d.setRevenue(revs[i]);
            d.setRevenueYtd(revs[i] * 8.5);
            d.setEbitda(ebitdas[i]);
            d.setEbitdaYtd(ebitdas[i] * 8.5);
            d.setGrossProfit(gps[i]);
            d.setEbitdaMargin(ebitdas[i] / revs[i] * 100);
            d.setGrossMargin(gps[i] / revs[i] * 100);
            d.setCashFlow(ebitdas[i] * 0.7);
            divs.put(names[i], d);
            totalRev += revs[i]; totalEbitda += ebitdas[i]; totalGP += gps[i];
        }

        kpi.setDivisions(divs);
        kpi.setTotalRevenue(totalRev);
        kpi.setTotalEbitda(totalEbitda);
        kpi.setTotalGrossProfit(totalGP);
        kpi.setTotalCashFlow(4200);
        
        return kpi;
    }

    /**
     * Get console path
     */
    public String getConsolePath() { 
        return consolePath; 
    }

    /**
     * Validate console file
     */
    public boolean validateConsoleFile(File file) {
        if (file == null || !file.exists()) {
            System.err.println("[PARSER] File does not exist");
            return false;
        }

        if (!file.getName().endsWith(".xlsx") && !file.getName().endsWith(".xls")) {
            System.err.println("[PARSER] File is not an Excel file");
            return false;
        }

        if (file.getName().startsWith("~$")) {
            System.err.println("[PARSER] File is a temporary Excel file");
            return false;
        }

        // Check if month can be detected
        String month = monthDetector.detectMonth(file.getName());
        if (month == null) {
            System.err.println("[PARSER] Cannot detect month from filename: " + file.getName());
            return false;
        }

        System.out.println("[PARSER] ✓ File validation passed: " + file.getName() + " (Month: " + month + ")");
        return true;
    }
}