package com.ntg.reporting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * NTGConsoleWatcher - Monitors console folder and processes ALL valid Excel files.
 * Each file is tracked individually. All months merge into one dashboard file.
 */
@Service
public class NTGConsoleWatcher {

    @Autowired
    private DashboardGeneratorService dashboardGeneratorService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private MonthDetector monthDetector;

    private static final String CONSOLE_INPUT_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\Console file";

    private final Map<String, Long> processedFiles = new HashMap<>();
    private int checkCounter = 0;

    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Scheduled(fixedDelay = 5000)
    public void checkForUpdates() {
        checkCounter++;
        System.out.println("\n[WATCHER #" + checkCounter + "] Running at " +
                           TIME_FORMATTER.format(Instant.now()));
        try {
            File consoleFolder = new File(CONSOLE_INPUT_PATH);
            if (!consoleFolder.exists()) {
                System.err.println("[WATCHER] ❌ Console folder not found: " + CONSOLE_INPUT_PATH);
                return;
            }

            File[] excelFiles = consoleFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".xlsx") && !name.startsWith("~$")
            );

            if (excelFiles == null || excelFiles.length == 0) {
                System.out.println("[WATCHER] ℹ No Excel files found in console folder");
                return;
            }

            System.out.println("[WATCHER] Found " + excelFiles.length + " Excel file(s)");
            Arrays.sort(excelFiles, Comparator.comparingLong(File::lastModified));

            for (File file : excelFiles) {
                processFileIfNew(file);
            }

            if (checkCounter % 60 == 0) {
                printDetailedStatistics();
            }

        } catch (Exception e) {
            System.err.println("[WATCHER] ❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processFileIfNew(File file) {
        String name         = file.getName();
        long   lastModified = file.lastModified();

        String detectedMonth = monthDetector.detectMonth(name);
        if (detectedMonth == null) {
            System.err.println("[WATCHER] ⚠ Cannot detect month from: " + name + " — skipping");
            return;
        }

        var mappingsForMonth = mappingService.getMappingsByMonth(detectedMonth);
        if (mappingsForMonth.isEmpty()) {
            System.err.println("[WATCHER] ⚠ No mappings for month: " + detectedMonth + " — skipping");
            return;
        }

        Long prev = processedFiles.get(name);
        if (prev != null && prev == lastModified) {
            System.out.println("[WATCHER] ℹ No changes: " + name);
            return;
        }

        System.out.println("\n[WATCHER] 🔄 Processing: " + name);
        System.out.println("[WATCHER] Month: " + detectedMonth + " | Mappings: " + mappingsForMonth.size());

        try {
            long startTime = System.currentTimeMillis();
            dashboardGeneratorService.generateDashboardFromConsole(file.getAbsolutePath());
            long duration = System.currentTimeMillis() - startTime;
            processedFiles.put(name, lastModified);
            System.out.println("[WATCHER] ✅ Done: " + name + " (" + detectedMonth + ") in " + duration + "ms");
        } catch (Exception e) {
            System.err.println("[WATCHER] ❌ Error processing " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printDetailedStatistics() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              MAPPING & SYSTEM STATISTICS                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n📊 Total Mappings: " + mappingService.getTotalMappings());
        var stats = mappingService.getStatistics();
        System.out.println("\n📅 Month Coverage:");
        System.out.println("   Available: " + stats.get("monthsList"));
        System.out.println("\n⚙️  Watcher:");
        System.out.println("   Checks: " + checkCounter);
        System.out.println("   Processed months: " + getProcessedMonths());
        System.out.println("\n════════════════════════════════════════════════════════════\n");
    }

    private String getProcessedMonths() {
        if (processedFiles.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (String filename : processedFiles.keySet()) {
            String month = monthDetector.detectMonth(filename);
            if (month != null) sb.append(month).append(" ");
        }
        return sb.toString().trim();
    }

    public void triggerManualGeneration() {
        System.out.println("\n[WATCHER] Manual generation triggered");
        checkForUpdates();
    }

    public Map<String, Object> getWatcherStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("checkCounter",    checkCounter);
        status.put("processedFiles",  processedFiles.keySet());
        status.put("processedMonths", getProcessedMonths());
        status.put("consolePath",     CONSOLE_INPUT_PATH);
        status.put("totalMappings",   mappingService.getTotalMappings());
        return status;
    }

    public void resetWatcher() {
        System.out.println("[WATCHER] Resetting watcher state...");
        processedFiles.clear();
        checkCounter = 0;
        System.out.println("[WATCHER] ✓ Reset complete");
    }
}