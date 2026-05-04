package com.ntg.reporting.service;

import com.ntg.reporting.model.KpiData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * FileWatcherService - Enhanced with MonthDetector validation
 * 
 * Monitors console files and refreshes KPI data when files change
 * Now validates files using MonthDetector before processing
 */
@Service
public class FileWatcherService {

    @Autowired
    private ExcelParserService excelParserService;

    @Autowired
    private MonthDetector monthDetector;

    private KpiData cachedData = null;
    private long lastModified = 0;
    private String lastProcessedFileName = "";

    /**
     * Watch console file for changes every 30 seconds
     * ENHANCED: Validates files with MonthDetector before processing
     */
    @Scheduled(fixedDelay = 30000) // runs every 30 seconds
    public void watchConsolFile() {
        try {
            File dir = new File(excelParserService.getConsolePath());
            
            if (!dir.exists() || !dir.isDirectory()) {
                System.err.println("[FILE_WATCHER] Console directory not found: " + dir.getAbsolutePath());
                return;
            }

            // Find Excel files, excluding temp files
            File[] files = dir.listFiles((d, name) -> 
                (name.endsWith(".xlsx") || name.endsWith(".xls")) && !name.startsWith("~$")
            );

            if (files == null || files.length == 0) {
                if (cachedData == null) {
                    cachedData = excelParserService.getMockData();
                }
                return;
            }

            // Find latest file
            long latestModified = 0;
            File latestFile = null;
            
            for (File f : files) {
                if (f.lastModified() > latestModified) {
                    latestModified = f.lastModified();
                    latestFile = f;
                }
            }

            if (latestFile == null) {
                return;
            }

            // ============================================================
            // ENHANCED: Validate file with MonthDetector
            // ============================================================
            String detectedMonth = monthDetector.detectMonth(latestFile.getName());
            
            if (detectedMonth == null) {
                System.err.println("[FILE_WATCHER] ⚠ File does not match expected naming pattern: " + latestFile.getName());
                System.err.println("[FILE_WATCHER] Expected: '... - [MonthName]_BR.xlsx'");
                return;
            }

            // Check if file has changed
            boolean fileChanged = latestModified > lastModified;
            boolean differentFile = !latestFile.getName().equals(lastProcessedFileName);

            if (fileChanged || differentFile || cachedData == null) {
                System.out.println("\n[FILE_WATCHER] ============================================");
                System.out.println("[FILE_WATCHER] Console file detected: " + latestFile.getName());
                System.out.println("[FILE_WATCHER] Detected month: " + detectedMonth);
                System.out.println("[FILE_WATCHER] File modified: " + new java.util.Date(latestModified));
                
                lastModified = latestModified;
                lastProcessedFileName = latestFile.getName();
                
                // Refresh KPI data
                cachedData = excelParserService.parseConsoleFile();
                
                System.out.println("[FILE_WATCHER] ✅ KPIs refreshed successfully");
                System.out.println("[FILE_WATCHER] Month: " + cachedData.getMonth());
                System.out.println("[FILE_WATCHER] ============================================\n");
            }

        } catch (Exception e) {
            System.err.println("[FILE_WATCHER] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get cached KPI data
     */
    public KpiData getCachedData() {
        if (cachedData == null) {
            cachedData = excelParserService.parseConsoleFile();
        }
        return cachedData;
    }

    /**
     * Force refresh of KPI data
     */
    public KpiData forceRefresh() {
        System.out.println("[FILE_WATCHER] Force refresh requested");
        cachedData = excelParserService.parseConsoleFile();
        return cachedData;
    }

    /**
     * Get last processed file info
     */
    public java.util.Map<String, Object> getWatcherInfo() {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("lastProcessedFile", lastProcessedFileName);
        info.put("lastModified", lastModified);
        info.put("lastModifiedDate", new java.util.Date(lastModified).toString());
        info.put("cacheStatus", cachedData != null ? "loaded" : "empty");
        
        if (cachedData != null) {
            info.put("cachedMonth", cachedData.getMonth());
        }
        
        return info;
    }

    /**
     * Reset watcher state
     */
    public void reset() {
        System.out.println("[FILE_WATCHER] Resetting watcher state...");
        lastModified = 0;
        lastProcessedFileName = "";
        cachedData = null;
        System.out.println("[FILE_WATCHER] ✓ Reset complete");
    }
}