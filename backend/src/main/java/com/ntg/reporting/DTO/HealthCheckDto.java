package com.ntg.reporting.dto;

public class HealthCheckDto {
    private String status;
    private String message;
    private long timestamp;
    private long mappingsLoaded;

    public HealthCheckDto() {
        this.timestamp = System.currentTimeMillis();
    }

    public HealthCheckDto(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getMappingsLoaded() {
        return mappingsLoaded;
    }

    public void setMappingsLoaded(long mappingsLoaded) {
        this.mappingsLoaded = mappingsLoaded;
    }
}