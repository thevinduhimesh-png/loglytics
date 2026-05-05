# Live Financial reporting system

## How to Start

### First time only:
1. Install Java JDK 21 from https://adoptium.net
2. Install Maven from https://maven.apache.org
3. Install Node.js from https://nodejs.org

### Every time:
1. Double-click START-BACKEND.bat  (wait for "Started NtgReportingApplication")
2. Double-click START-FRONTEND.bat (browser opens automatically)
3. Go to http://localhost:3000

## Add your data:
- Drop your NTG console Excel file into the /console-files/ folder
- The dashboard auto-refreshes every 30 seconds

## Folder structure:
- /backend         → Java Spring Boot API (port 8080)
- /frontend        → React dashboard (port 3000)
- /console-files   → Put your .xlsx console file here
