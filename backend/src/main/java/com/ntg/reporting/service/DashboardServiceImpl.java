package com.ntg.reporting.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ntg.reporting.dto.DashboardStatusDto;
import com.ntg.reporting.dto.WatcherStatusDto;
import java.nio.file.Path;
import java.util.Map;

/**
 * Dashboard Service Implementation
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DashboardServiceImpl.class);
    
    private static final String OUTPUT_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\The end result";
    private static final String CONSOLE_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\Console";
    private static final String POWER_BI_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\NTG_Dashboard.pbit";
    private static final String DASHBOARD_FILE = "NTG_2025_Dashboard.xlsx";
    
    private java.util.List<String> processedFiles = new java.util.ArrayList<>();
    private java.util.Set<String> processedMonths = new java.util.HashSet<>();
    private int checkCounter = 0;
    private java.util.Date lastCheckTime = new java.util.Date();
    private long mappingsCount = 23600;

    @Override
    public String processConsoleFile(MultipartFile file) throws Exception {
        try {
            String filename = file.getOriginalFilename();
            
            // Validate file
            if (!filename.endsWith(".xlsx")) {
                throw new IllegalArgumentException("Only .xlsx files supported");
            }
            
            // Extract month from filename
            String month = extractMonth(filename);
            if (month == null) {
                throw new IllegalArgumentException("Could not detect month from filename. Expected format: *_January_* or similar");
            }
            
            logger.info("Processing file: {} (Month: {})", filename, month);
            
            // Save file
            java.nio.file.Path outputDir = java.nio.file.Paths.get(OUTPUT_PATH);
            if (!java.nio.file.Files.exists(outputDir)) {
                java.nio.file.Files.createDirectories(outputDir);
            }
            
            java.nio.file.Path filePath = outputDir.resolve(filename);
            java.nio.file.Files.write(filePath, file.getBytes());
            
            // Track processed data
            if (!processedFiles.contains(filename)) {
                processedFiles.add(filename);
            }
            processedMonths.add(month);
            checkCounter++;
            lastCheckTime = new java.util.Date();
            
            logger.info("File processed: {} | Months: {} | Total files: {}", 
                filename, String.join(" ", processedMonths), processedFiles.size());
            
            return "Successfully processed " + filename + " for month " + month;
            
        } catch (Exception e) {
            logger.error("Process file error", e);
            throw e;
        }
    }

    @Override
    public DashboardStatusDto getDashboardStatus() throws Exception {
        DashboardStatusDto status = new DashboardStatusDto();
        
        WatcherStatusDto watcherStatus = new WatcherStatusDto();
        watcherStatus.setConsolePath(CONSOLE_PATH);
        watcherStatus.setProcessedMonths(String.join(" ", processedMonths));
        watcherStatus.setProcessedFiles(new java.util.ArrayList<>(processedFiles));
        watcherStatus.setCheckCounter(checkCounter);
        watcherStatus.setLastCheck(lastCheckTime);
        watcherStatus.setStatus("active");
        
        status.setWatcherStatus(watcherStatus);
        status.setOnline(true);
        
        // Add metrics
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("processedMonths", processedMonths.size());
        metrics.put("totalFiles", processedFiles.size());
        metrics.put("mappingsLoaded", mappingsCount);
        metrics.put("entities", 9);
        metrics.put("completion", Math.round((processedMonths.size() / 12.0) * 100));
        status.setMetrics(metrics);
        
        return status;
    }

    @Override
    public long getMappingsCount() throws Exception {
        return mappingsCount;
    }

    @Override
    public void resetWatcherState() throws Exception {
        processedFiles.clear();
        processedMonths.clear();
        checkCounter = 0;
        lastCheckTime = new java.util.Date();
        logger.info("Watcher state reset");
    }

    @Override
    public void openPowerBITemplate() throws Exception {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: Use ms-powerbi protocol
                String command = "ms-powerbi:open?filePath=" + POWER_BI_PATH;
                java.awt.Desktop.getDesktop().browse(new java.net.URI(command));
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec(new String[]{"open", POWER_BI_PATH});
            } else {
                // Linux
                Runtime.getRuntime().exec(new String[]{"xdg-open", POWER_BI_PATH});
            }
        } catch (Exception e) {
            logger.warn("Could not open Power BI", e);
            throw new RuntimeException("Power BI template not available on this platform");
        }
    }

    @Override
    public void openOutputFolder() throws Exception {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", OUTPUT_PATH});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", OUTPUT_PATH});
            } else {
                Runtime.getRuntime().exec(new String[]{"nautilus", OUTPUT_PATH});
            }
        } catch (Exception e) {
            logger.warn("Could not open folder", e);
            throw new RuntimeException("Could not open output folder");
        }
    }

    @Override
    public Path exportDashboard() throws Exception {
        java.nio.file.Path dashboardPath = java.nio.file.Paths.get(OUTPUT_PATH, DASHBOARD_FILE);
        
        if (!java.nio.file.Files.exists(dashboardPath)) {
            throw new RuntimeException("Dashboard file not found: " + dashboardPath);
        }
        
        return dashboardPath;
    }

    @Override
    public Map<String, Object> getComprehensiveMetrics() throws Exception {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        
        metrics.put("monthsProcessed", processedMonths.size());
        metrics.put("filesProcessed", processedFiles.size());
        metrics.put("mappingsLoaded", mappingsCount);
        metrics.put("entitiesCovered", 9);
        metrics.put("completionPercent", Math.round((processedMonths.size() / 12.0) * 100));
        metrics.put("checksPerformed", checkCounter);
        metrics.put("lastUpdate", lastCheckTime);
        metrics.put("status", "online");
        
        // Finance-specific metrics
        metrics.put("consolidatedMonths", new java.util.ArrayList<>(processedMonths));
        metrics.put("dataPoints", mappingsCount);
        metrics.put("processingRate", "5s cycle");
        
        return metrics;
    }

    @Override
    public Map<String, Object> getProcessingHistory() throws Exception {
        java.util.Map<String, Object> history = new java.util.HashMap<>();
        
        history.put("totalFiles", processedFiles.size());
        history.put("files", processedFiles);
        history.put("months", new java.util.ArrayList<>(processedMonths));
        history.put("lastCheck", lastCheckTime);
        history.put("checkCounter", checkCounter);
        
        return history;
    }

    // ── Helper Methods ───────────────────────────────────────────────
    
    /**
     * Extract month from filename
     * Handles formats like: "FY25_-_04_-_NSG_(Bermuda)_LP_Consolidation_-_April_BR_..."
     */
    private String extractMonth(String filename) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        String[] shortMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Try full month names first
        for (int i = 0; i < months.length; i++) {
            if (filename.toUpperCase().contains(months[i].toUpperCase())) {
                return shortMonths[i];
            }
        }
        
        // Try short month names
        for (String shortMonth : shortMonths) {
            if (filename.toUpperCase().contains(shortMonth.toUpperCase())) {
                return shortMonth;
            }
        }
        
        return null;
    }
}
