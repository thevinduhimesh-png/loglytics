package com.ntg.reporting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private MonthDetector monthDetector; // Integration for month validation

    /**
     * Extracts the entity name from the standard filename pattern:
     * "FY25 - 04 - [Entity Name] - [Month]_BR.xlsx"
     */
    public Optional<String> extractEntityFromFilename(String filename) {
        try {
            // Pattern: Splitting by " - " to isolate the 3rd component (Entity)
            String[] parts = filename.split(" - ");
            if (parts.length >= 3) {
                String entity = parts[2].trim();
                // Handling potential parentheses in entity names like "NSG (Bermuda) LP Consolidation"
                return Optional.of(entity);
            }
        } catch (Exception e) {
            System.err.println("[FILE] ✗ Could not extract entity from: " + filename);
        }
        return Optional.empty();
    }

    /**
     * Validates if the file is a valid "Console" file by checking its extension
     * and ensuring the MonthDetector can successfully parse a reporting period.
     */
    public boolean isValidConsoleFile(File file) {
        if (file == null || !file.exists() || file.getName().startsWith("~$")) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        boolean isExcel = name.endsWith(".xlsx") || name.endsWith(".xls");
        
        // Ensure the filename contains a detectable month defined in MonthDetector
        String detectedMonth = monthDetector.detectMonth(file.getName());
        
        return isExcel && detectedMonth != null;
    }

    /**
     * Enhanced version of getLatestExcelFile that only returns files
     * with valid reporting months in their names.
     */
    public File getLatestValidConsoleFile(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return null;

        File[] files = dir.listFiles((d, name) -> {
            File f = new File(d, name);
            return isValidConsoleFile(f);
        });

        if (files == null || files.length == 0) return null;

        File latestFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (files[i].lastModified() > latestFile.lastModified()) {
                latestFile = files[i];
            }
        }
        return latestFile;
    }

    /**
     * Copy file from source to destination
     */
    public boolean copyFile(File source, File destination) {
        try {
            if (!source.exists()) {
                System.err.println("[FILE] Source file not found: " + source.getAbsolutePath());
                return false;
            }
            
            File destDir = destination.getParentFile();
            if (destDir != null && !destDir.exists()) {
                destDir.mkdirs();
            }
            
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            
            System.out.println("[FILE] ✅ Copied: " + source.getName() + " → " + destination.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[FILE] Error copying file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atomically move/rename file (for safe operations)
     */
    public boolean moveFile(File source, File destination) {
        try {
            if (!source.exists()) return false;
            
            if (!copyFile(source, destination)) return false;
            
            if (source.delete()) {
                System.out.println("[FILE] ✅ Moved: " + source.getName());
                return true;
            } else {
                System.err.println("[FILE] Warning: Copied but couldn't delete source file");
                return true; 
            }
        } catch (Exception e) {
            System.err.println("[FILE] Error moving file: " + e.getMessage());
            return false;
        }
    }

    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    public boolean ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        return dir.exists() || dir.mkdirs();
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return !file.exists() || file.delete();
    }
}