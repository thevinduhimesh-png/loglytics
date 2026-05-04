package com.ntg.reporting.dto;

import java.util.Date;
import java.util.List;

public class WatcherStatusDto {
    private String consolePath;
    private String processedMonths;
    private List<String> processedFiles;
    private int checkCounter;
    private Date lastCheck;
    private String status;

    public WatcherStatusDto() {
    }

    public String getConsolePath() {
        return consolePath;
    }

    public void setConsolePath(String consolePath) {
        this.consolePath = consolePath;
    }

    public String getProcessedMonths() {
        return processedMonths;
    }

    public void setProcessedMonths(String processedMonths) {
        this.processedMonths = processedMonths;
    }

    public List<String> getProcessedFiles() {
        return processedFiles;
    }

    public void setProcessedFiles(List<String> processedFiles) {
        this.processedFiles = processedFiles;
    }

    public int getCheckCounter() {
        return checkCounter;
    }

    public void setCheckCounter(int checkCounter) {
        this.checkCounter = checkCounter;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
