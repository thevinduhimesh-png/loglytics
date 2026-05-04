package com.ntg.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NtgReportingApplication {
    public static void main(String[] args) {
        SpringApplication.run(NtgReportingApplication.class, args);
    }
}
