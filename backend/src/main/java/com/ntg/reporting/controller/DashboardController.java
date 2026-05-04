package com.ntg.reporting.controller;

import com.ntg.reporting.service.NTGConsoleWatcher;
import com.ntg.reporting.service.MappingService;
import com.ntg.reporting.service.MonthDetector;
import com.ntg.reporting.service.DashboardGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardController - REST API for the NTG Reporting frontend.
 *
 * Endpoints:
 * POST /api/dashboard/upload              - Upload and process a console file
 * GET  /api/dashboard/health              - Health check
 * GET  /api/dashboard/status              - Watcher + system status
 * GET  /api/dashboard/mappings/stats      - Mapping statistics
 * GET  /api/dashboard/mappings/by-month/{month} - Mappings for a month
 * GET  /api/dashboard/month/detect        - Detect month from filename
 * GET  /api/dashboard/month/validate      - Validate console filename
 * GET  /api/dashboard/months              - All available months
 * GET  /api/dashboard/mapping             - Specific cell mapping
 * POST /api/dashboard/generate            - Manually trigger generation
 * POST /api/dashboard/validate-file       - Validate a file path
 * POST /api/dashboard/watcher/reset       - Reset watcher state
 * GET  /api/dashboard/info                - System info
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private NTGConsoleWatcher watcher;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private MonthDetector monthDetector;

    @Autowired
    private DashboardGeneratorService dashboardGeneratorService;

    private static final String CONSOLE_FOLDER = "C:\\Users\\Thevindu.Aditya\\Desktop\\Console file";

    // ── Upload endpoint ───────────────────────────────────────────────
    /**
     * Receives a console file from the web UI, saves it to the console folder,
     * and immediately triggers dashboard generation.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadConsoleFile(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                response.put("success", false);
                response.put("message", "Only .xlsx files are supported");
                return ResponseEntity.badRequest().body(response);
            }

            // Save to console folder
            String consolePath = CONSOLE_FOLDER + "\\" + file.getOriginalFilename();
            File dest = new File(consolePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            System.out.println("[API] File saved: " + consolePath);

            // Immediately generate dashboard
            dashboardGeneratorService.generateDashboardFromConsole(consolePath);

            response.put("success", true);
            response.put("message", "File processed successfully");
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[API] Upload error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Health check ──────────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("status", "healthy");
            response.put("service", "NTG Reporting System");
            response.put("version", "2.0");
            response.put("timestamp", java.time.Instant.now().toString());
            int totalMappings = mappingService.getTotalMappings();
            response.put("mappingsLoaded", String.valueOf(totalMappings));
            response.put("monthDetectorActive", monthDetector != null ? "true" : "false");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    // ── System status ─────────────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "running");
            response.put("timestamp", java.time.Instant.now().toString());
            response.put("totalMappings", mappingService.getTotalMappings());
            response.put("mappingStats", mappingService.getStatistics());
            response.put("watcherStatus", watcher.getWatcherStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Mapping stats ─────────────────────────────────────────────────
    @GetMapping("/mappings/stats")
    public ResponseEntity<Map<String, Object>> getMappingStats() {
        try {
            Map<String, Object> stats = mappingService.getStatistics();
            stats.put("totalMappings", mappingService.getTotalMappings());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ── Mappings by month ─────────────────────────────────────────────
    @GetMapping("/mappings/by-month/{month}")
    public ResponseEntity<Map<String, Object>> getMappingsByMonth(@PathVariable String month) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!monthDetector.isValidMonth(month)) {
                response.put("error", "Invalid month: " + month);
                response.put("validMonths", List.of("Jan","Feb","Mar","Apr","May","Jun",
                                                    "Jul","Aug","Sep","Oct","Nov","Dec"));
                return ResponseEntity.badRequest().body(response);
            }
            List<Object[]> mappings = mappingService.getMappingsByMonth(month);
            response.put("month", month);
            response.put("count", mappings.size());
            response.put("mappings", mappings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Detect month ──────────────────────────────────────────────────
    @GetMapping("/month/detect")
    public ResponseEntity<Map<String, Object>> detectMonth(@RequestParam String filename) {
        Map<String, Object> response = new HashMap<>();
        try {
            String detectedMonth = monthDetector.detectMonth(filename);
            response.put("filename", filename);
            response.put("detectedMonth", detectedMonth);
            response.put("success", detectedMonth != null);
            if (detectedMonth != null) {
                int mappingCount = mappingService.getMappingsByMonth(detectedMonth).size();
                response.put("mappingsAvailable", mappingCount);
            } else {
                response.put("message", "Could not detect month from filename");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Validate filename ─────────────────────────────────────────────
    @GetMapping("/month/validate")
    public ResponseEntity<Map<String, Object>> validateFilename(@RequestParam String filename) {
        Map<String, Object> response = new HashMap<>();
        try {
            String detectedMonth = monthDetector.detectMonth(filename);
            boolean isValid = detectedMonth != null;
            response.put("filename", filename);
            response.put("isValid", isValid);
            response.put("detectedMonth", detectedMonth);
            if (isValid) {
                int mappingCount = mappingService.getMappingsByMonth(detectedMonth).size();
                response.put("mappingsAvailable", mappingCount);
                response.put("message", "Valid console filename");
            } else {
                response.put("message", "Invalid filename pattern");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Available months ──────────────────────────────────────────────
    @GetMapping("/months")
    public ResponseEntity<Map<String, Object>> getAvailableMonths() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = mappingService.getStatistics();
            response.put("uniqueMonths", stats.get("uniqueMonths"));
            response.put("monthsList", stats.get("monthsList"));
            response.put("totalMappings", mappingService.getTotalMappings());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Specific mapping ──────────────────────────────────────────────
    @GetMapping("/mapping")
    public ResponseEntity<Map<String, Object>> getMapping(
            @RequestParam String entity,
            @RequestParam String month,
            @RequestParam String consoleCol,
            @RequestParam Integer consoleRow) {
        Map<String, Object> response = new HashMap<>();
        try {
            String[] mapping = mappingService.getMapping(entity, month, consoleCol, consoleRow);
            response.put("entity", entity);
            response.put("month", month);
            response.put("consoleCell", consoleCol + consoleRow);
            if (mapping != null) {
                response.put("found", true);
                response.put("dashboardRow", mapping[0]);
                response.put("dashboardCol", mapping[1]);
            } else {
                response.put("found", false);
                response.put("message", "No mapping found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Manual trigger ────────────────────────────────────────────────
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> triggerGeneration() {
        Map<String, Object> response = new HashMap<>();
        try {
            watcher.triggerManualGeneration();
            response.put("status", "triggered");
            response.put("message", "Dashboard generation triggered");
            response.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Validate file path ────────────────────────────────────────────
    @PostMapping("/validate-file")
    public ResponseEntity<Map<String, Object>> validateFile(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String filePath = request.get("filePath");
            if (filePath == null || filePath.isEmpty()) {
                response.put("valid", false);
                response.put("error", "File path is required");
                return ResponseEntity.badRequest().body(response);
            }
            boolean isValid = dashboardGeneratorService.validateConsoleFile(filePath);
            response.put("filePath", filePath);
            response.put("valid", isValid);
            response.put("message", isValid ? "Console file is valid" : "Validation failed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Reset watcher ─────────────────────────────────────────────────
    @PostMapping("/watcher/reset")
    public ResponseEntity<Map<String, Object>> resetWatcher() {
        Map<String, Object> response = new HashMap<>();
        try {
            watcher.resetWatcher();
            response.put("status", "success");
            response.put("message", "Watcher state has been reset");
            response.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── System info ───────────────────────────────────────────────────
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("service", "NTG Reporting System");
            response.put("version", "2.0 Enhanced");
            response.put("features", List.of(
                "Automatic month detection from filenames",
                "23,610 console-to-dashboard cell mappings",
                "Real-time file monitoring",
                "Atomic file operations",
                "REST API endpoints",
                "Multi-month single dashboard output"
            ));
            response.put("components", Map.of(
                "MonthDetector",      "Detects month from console filenames",
                "MappingService",     "Manages 23,610 cell mappings",
                "DashboardGenerator", "Generates dashboards from console files",
                "FileWatcher",        "Monitors console folder for changes"
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Open Power BI ─────────────────────────────────────────────────
    @PostMapping("/open-powerbi")
    public ResponseEntity<Map<String, Object>> openPowerBI() {
        Map<String, Object> response = new HashMap<>();
        try {
            String pbiPath = "C:\\Users\\Thevindu.Aditya\\Desktop\\NTG_Dashboard.pbit";
            Runtime.getRuntime().exec("cmd /c start \"\" \"" + pbiPath + "\"");
            response.put("success", true);
            response.put("message", "Power BI template opened");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── Open output folder ────────────────────────────────────────────
    @PostMapping("/open-folder")
    public ResponseEntity<Map<String, Object>> openOutputFolder() {
        Map<String, Object> response = new HashMap<>();
        try {
            String outputPath = "C:\\Users\\Thevindu.Aditya\\Desktop\\The end result";
            Runtime.getRuntime().exec("explorer.exe \"" + outputPath + "\"");
            response.put("success", true);
            response.put("message", "Output folder opened");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}