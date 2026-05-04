package com.ntg.reporting.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MonthDetector - Detects month from console filename
 * 
 * Filename Pattern: "FY25 - 04 - NSG (Bermuda) LP Consolidation - [MONTH_NAME]_BR.xlsx"
 * Example: "FY25 - 04 - NSG (Bermuda) LP Consolidation - April_BR.xlsx" → "Apr"
 */
@Service
public class MonthDetector {

    private static final Map<String, String> MONTH_NAME_TO_ABBREV = new HashMap<>();
    
    static {
        MONTH_NAME_TO_ABBREV.put("january", "Jan");
        MONTH_NAME_TO_ABBREV.put("february", "Feb");
        MONTH_NAME_TO_ABBREV.put("march", "Mar");
        MONTH_NAME_TO_ABBREV.put("april", "Apr");
        MONTH_NAME_TO_ABBREV.put("may", "May");
        MONTH_NAME_TO_ABBREV.put("june", "Jun");
        MONTH_NAME_TO_ABBREV.put("july", "Jul");
        MONTH_NAME_TO_ABBREV.put("august", "Aug");
        MONTH_NAME_TO_ABBREV.put("september", "Sep");
        MONTH_NAME_TO_ABBREV.put("october", "Oct");
        MONTH_NAME_TO_ABBREV.put("november", "Nov");
        MONTH_NAME_TO_ABBREV.put("december", "Dec");
    }

    /**
     * Detect month from console filename
     * 
     * @param filename Console file name
     * @return Month abbreviation (Jan, Feb, Mar, ..., Dec) or null if not detected
     * 
     * Examples:
     *   "FY25 - 04 - NSG (Bermuda) LP Consolidation - April_BR.xlsx" → "Apr"
     *   "FY25 - 04 - NSG (Bermuda) LP Consolidation - December_BR.xlsx" → "Dec"
     *   "FY25 - 04 - NSG (Bermuda) LP Consolidation - January_BR.xlsx" → "Jan"
     */
    public String detectMonth(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.err.println("[MONTH] ERROR: Filename is null or empty");
            return null;
        }

        System.out.println("[MONTH] Detecting month from: " + filename);

        // Pattern: Extract text between last dash and _BR
        // Example: "... - April_BR.xlsx" → "April"
        Pattern pattern = Pattern.compile("-\\s*([a-z]+)\\s*_br", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            String monthName = matcher.group(1).toLowerCase().trim();
            String monthAbbrev = MONTH_NAME_TO_ABBREV.get(monthName);
            
            if (monthAbbrev != null) {
                System.out.println("[MONTH] ✓ DETECTED: '" + monthName + "' → '" + monthAbbrev + "'");
                return monthAbbrev;
            }
        }

        // If pattern doesn't match, try simpler approach: look for month names anywhere
        String lowerFilename = filename.toLowerCase();
        for (Map.Entry<String, String> entry : MONTH_NAME_TO_ABBREV.entrySet()) {
            if (lowerFilename.contains(entry.getKey())) {
                System.out.println("[MONTH] ✓ DETECTED: '" + entry.getKey() + "' → '" + entry.getValue() + "'");
                return entry.getValue();
            }
        }

        System.err.println("[MONTH] ✗ FAILED: Could not detect month from filename: " + filename);
        return null;
    }

    /**
     * Get month abbreviation from full month name
     */
    public String getMonthAbbrev(String monthName) {
        return MONTH_NAME_TO_ABBREV.get(monthName.toLowerCase());
    }

    /**
     * Validate if a string is a valid month abbreviation
     */
    public boolean isValidMonth(String month) {
        return MONTH_NAME_TO_ABBREV.containsValue(month);
    }
}