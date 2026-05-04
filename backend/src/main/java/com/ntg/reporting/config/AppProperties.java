package com.ntg.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String outputDirectory;
    private String consoleDirectory;
    private String powerbiTemplate;

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getConsoleDirectory() {
        return consoleDirectory;
    }

    public void setConsoleDirectory(String consoleDirectory) {
        this.consoleDirectory = consoleDirectory;
    }

    public String getPowerbiTemplate() {
        return powerbiTemplate;
    }

    public void setPowerbiTemplate(String powerbiTemplate) {
        this.powerbiTemplate = powerbiTemplate;
    }
}
