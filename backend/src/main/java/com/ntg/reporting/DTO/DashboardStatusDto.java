package com.ntg.reporting.dto;

import java.util.Map;

public class DashboardStatusDto {
    private boolean online;
    private WatcherStatusDto watcherStatus;
    private Map<String, Object> metrics;

    public DashboardStatusDto() {
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public WatcherStatusDto getWatcherStatus() {
        return watcherStatus;
    }

    public void setWatcherStatus(WatcherStatusDto watcherStatus) {
        this.watcherStatus = watcherStatus;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
}
