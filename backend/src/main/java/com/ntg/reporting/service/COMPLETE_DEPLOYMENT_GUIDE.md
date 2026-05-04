# NTG Reporting System - Complete Service Layer Implementation Guide

## 📋 Overview

This guide covers all the service files needed for the Excel dashboard generation system.

---

## 🏗️ Service Architecture

```
NTGConsoleWatcher (Main Orchestrator)
├── Watches console file folder (5 sec intervals)
├── Calls DashboardGeneratorService
│   ├── Uses MappingService (1520 mappings)
│   ├── Uses ExcelService (utilities)
│   └── Generates dashboard Excel
├── Calls FileService
│   └── Copies PPT template
└── Reports status
```

---

## 📦 Files to Create

### 1. **MappingService.java**
**Location:** `src/main/java/com/ntg/reporting/service/MappingService.java`

**Purpose:**
- Stores all 1,520 data mappings (embedded, no external file needed)
- Provides mapping lookup methods
- Handles cell reference parsing (D10 → [row=9, col=3])
- Column index conversion (A-Z, AA-ZZ, etc.)

**Key Methods:**
- `getAllMappings()` - Get all mappings
- `getMappingsByEntity(String)` - Filter by entity
- `getMappingsByLineItem(String)` - Filter by line item
- `parseCellReference(String)` - Parse cell reference
- `convertColumnToIndex(String)` - Convert column letter to index

---

### 2. **ExcelService.java**
**Location:** `src/main/java/com/ntg/reporting/service/ExcelService.java`

**Purpose:**
- Utility functions for Excel operations
- Cell value extraction (handles numeric, formula, string types)
- Cell value setting
- Currency formatting

**Key Methods:**
- `getCellNumericValue(Cell)` - Extract numeric value
- `getCellStringValue(Cell)` - Extract text value
- `setCellNumericValue(Cell, double)` - Set numeric value
- `setCellStringValue(Cell, String)` - Set text value
- `isCellEmpty(Cell)` - Check if empty
- `formatCurrency(double)` - Format as currency ($X,XXX)

---

### 3. **DashboardGeneratorService.java**
**Location:** `src/main/java/com/ntg/reporting/service/DashboardGeneratorService.java`

**Purpose:**
- Main dashboard generation logic
- Reads console Excel file
- Applies all 1,520 mappings
- Writes output dashboard Excel
- Returns generation result with stats

**Key Methods:**
- `generateDashboard(File, File, File)` - Main generation method
- Returns `DashboardGenerationResult` with:
  - Success/failure status
  - Success and failure counts
  - Duration in milliseconds
  - Success rate percentage

---

### 4. **FileService.java**
**Location:** `src/main/java/com/ntg/reporting/service/FileService.java`

**Purpose:**
- File system operations
- File copying, moving, deletion
- Directory creation
- File discovery (latest file, Excel files only)

**Key Methods:**
- `copyFile(File, File)` - Copy file
- `moveFile(File, File)` - Move/rename file
- `fileExists(String)` - Check file existence
- `ensureDirectoryExists(String)` - Create directory if needed
- `getLatestFile(String)` - Get most recently modified file
- `getLatestExcelFile(String)` - Get latest Excel file (ignores temp files)
- `deleteFile(String)` - Delete file
- `getFileSize(String)` - Get file size in bytes
- `getFileSizeMB(String)` - Get file size in MB
- `getLastModified(String)` - Get last modification time

---

### 5. **NTGConsoleWatcher.java**
**Location:** `src/main/java/com/ntg/reporting/service/NTGConsoleWatcher.java`

**Purpose:**
- Main orchestrator service
- Scheduled file watcher (runs every 5 seconds)
- Detects console file changes
- Triggers dashboard generation
- Triggers PPT copying
- Provides status information

**Key Methods:**
- `watchConsoleFile()` - Scheduled task (runs every 5 seconds)
- `processConsoleFile(File)` - Process detected file change
- `generateDashboard(File)` - Generate dashboard
- `copyPowerPoint()` - Copy PPT template
- `getStatus()` - Get current status

**Configuration:**
```java
private static final String CONSOLE_INPUT_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\Console file";
private static final String OUTPUT_PATH = "C:\\Users\\Thevindu.Aditya\\Desktop\\The end result";
private static final String DASHBOARD_TEMPLATE = "C:\\Users\\Thevindu.Aditya\\Desktop\\Templates\\NTG 2025 Dashboard_Data Removed.xlsx";
private static final String PPT_TEMPLATE = "C:\\Users\\Thevindu.Aditya\\Desktop\\Templates\\12. NTG Dashboard December 2025.pptx";
```

---

### 6. **FileWatcherService.java**
**Location:** `src/main/java/com/ntg/reporting/service/FileWatcherService.java`

**Purpose:**
- Placeholder service for interface compatibility
- Actual functionality in NTGConsoleWatcher

**Content:**
```java
@Service
public class FileWatcherService {
    // Placeholder - functionality in NTGConsoleWatcher
}
```

---

## 🚀 Deployment Steps

### Step 1: Create Service Directory Structure
```
src/main/java/com/ntg/reporting/service/
├── MappingService.java
├── ExcelService.java
├── DashboardGeneratorService.java
├── FileService.java
├── NTGConsoleWatcher.java
└── FileWatcherService.java
```

### Step 2: Copy All Files
1. Copy `MappingService.java`
2. Copy `ExcelService.java`
3. Copy `DashboardGeneratorService.java`
4. Copy `FileService.java`
5. Copy `NTGConsoleWatcher_COMPLETE.java` → Rename to `NTGConsoleWatcher.java`
6. Copy `FileWatcherService_CORRECT.java` → Rename to `FileWatcherService.java`

### Step 3: Verify pom.xml Dependencies
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-scratchpad</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.xmlbeans</groupId>
    <artifactId>xmlbeans</artifactId>
    <version>5.1.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-scheduling</artifactId>
</dependency>
```

### Step 4: Build and Run
```bash
cd C:\Users\Thevindu.Aditya\Desktop\ntg-reporting\backend

# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

### Step 5: Verify Startup
You should see:
```
Started NtgReportingApplication
[CHECK #1] Watcher running at ...
```

---

## 📊 Data Flow

```
Console File (F_PL_YTD)
    ↓
NTGConsoleWatcher (detects change)
    ↓
DashboardGeneratorService
    ├── Read console file
    ├── Read dashboard template
    ├── Apply 1,520 mappings
    │   ├── MappingService (lookup mappings)
    │   ├── ExcelService (extract/set values)
    │   └── MappingService (parse cell refs)
    ├── Force formula recalculation
    └── Write dashboard output
        ↓
        Dashboard Excel (2025_YTD_A)
    ↓
FileService (copy PPT)
    ↓
    PPT Output
```

---

## 🔍 Mappings Summary

**Total Mappings:** 1,520

**Structure:**
- 9 Entities (A, B, C, D, E, F, G, H, Banking Group)
- ~190 Line items per mapping
- All from: F_PL_YTD → 2025_YTD_A

**Sample Mappings:**
```
Entity A Net Revenue (C12) → D14
Entity B Net Revenue (C12) → Q14
Entity G Net Revenue (C12) → CD14
Entity H Net Revenue (C12) → CQ14
Banking Group Net Revenue (C12) → DD14
```

---

## 📝 Configuration

All paths are hardcoded in `NTGConsoleWatcher.java`:

| Setting | Value |
|---------|-------|
| Console Input | `C:\Users\Thevindu.Aditya\Desktop\Console file` |
| Output Folder | `C:\Users\Thevindu.Aditya\Desktop\The end result` |
| Dashboard Template | `C:\Users\Thevindu.Aditya\Desktop\Templates\NTG 2025 Dashboard_Data Removed.xlsx` |
| PPT Template | `C:\Users\Thevindu.Aditya\Desktop\Templates\12. NTG Dashboard December 2025.pptx` |
| Watch Interval | 5 seconds |

---

## 🎯 Expected Output

### Console Output:
```
====================================================================
[CHECK #1] Watcher running at 2026-04-20 10:30:15
====================================================================
[WATCH] Checking folder: C:\Users\Thevindu.Aditya\Desktop\Console file
[WATCH] Latest file: Consol - Test.xlsx (2,834,567 bytes)

*** FILE CHANGED - PROCESSING ***

[PROCESS] Starting file processing...
[STEP 1] Generating Dashboard Excel...
[DASH] Starting dashboard generation...
[DASH] Applying 1520 mappings...
[DASH] ✅ Success: 1520 | Failed: 0
[DASH] Output: C:\Users\Thevindu.Aditya\Desktop\The end result\NTG 2025 Dashboard_LIVE.xlsx
[DASH] Time: 2345ms

[STEP 2] Copying PowerPoint template...
[PPT] Starting PowerPoint copy...
[PPT] ✅ PowerPoint copied successfully

====================================================================
✅ AUTOMATION COMPLETE!
====================================================================
[SUMMARY]
  Dashboard: C:\Users\Thevindu.Aditya\Desktop\The end result\NTG 2025 Dashboard_LIVE.xlsx
  Mappings: 1520 successful, 0 failed
  Success Rate: 100.0%
  Duration: 2345ms
====================================================================
```

### Output Files:
1. **Dashboard:** `C:\...\The end result\NTG 2025 Dashboard_LIVE.xlsx` (Updated immediately)
2. **PPT:** `C:\...\The end result\NTG Dashboard December 2025_LIVE.pptx` (Copied template)

---

## ✅ Verification Checklist

After deployment, verify:

- [ ] All 6 service files created in correct location
- [ ] Build completes successfully: `mvn clean install`
- [ ] Application starts: `mvn spring-boot:run`
- [ ] Console shows "Started NtgReportingApplication"
- [ ] Watcher shows "CHECK #1" running
- [ ] Modify console file
- [ ] Watcher detects change within 5 seconds
- [ ] Dashboard Excel generated in output folder
- [ ] PPT copied to output folder
- [ ] Console shows "1520 successful" mappings
- [ ] All output files have current timestamp

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails: "class NTGConsoleWatcher is public" | Make sure FileWatcherService is a separate file with FileWatcherService class |
| Build fails: "cannot find symbol getRowCount" | This error is already fixed - use latest version |
| Dashboard doesn't generate | Check console shows "*** FILE CHANGED" |
| Mappings show as failed | Check sheet names are correct: F_PL_YTD and 2025_YTD_A |
| Files not found errors | Verify paths exist on your system |

---

## 📞 Next Steps

1. ✅ Deploy all 6 service files
2. ✅ Build and verify compilation
3. ✅ Test with console file
4. ✅ Verify Excel generation (should be working now)
5. ⏳ Later: Add PPT dynamic generation
6. ⏳ Later: Add REST API controllers
7. ⏳ Later: Add React frontend

---

**Status:** Ready to deploy! 🚀
