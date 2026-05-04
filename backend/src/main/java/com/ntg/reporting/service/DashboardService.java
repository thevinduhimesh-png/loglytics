package com.ntg.reporting.service;

import org.springframework.web.multipart.MultipartFile;
import com.ntg.reporting.dto.DashboardStatusDto;
import java.nio.file.Path;
import java.util.Map;

/**
 * Dashboard Service Interface
 */
public interface DashboardService {
    
    /**
     * Process console file and map data to dashboard
     */
    String processConsoleFile(MultipartFile file) throws Exception;
    
    /**
     * Get current dashboard status
     */
    DashboardStatusDto getDashboardStatus() throws Exception;
    
    /**
     * Get count of loaded mappings
     */
    long getMappingsCount() throws Exception;
    
    /**
     * Reset watcher state
     */
    void resetWatcherState() throws Exception;
    
    /**
     * Open Power BI template
     */
    void openPowerBITemplate() throws Exception;
    
    /**
     * Open output folder
     */
    void openOutputFolder() throws Exception;
    
    /**
     * Export dashboard data
     */
    Path exportDashboard() throws Exception;
    
    /**
     * Get comprehensive metrics
     */
    Map<String, Object> getComprehensiveMetrics() throws Exception;
    
    /**
     * Get processing history
     */
    Map<String, Object> getProcessingHistory() throws Exception;
}
