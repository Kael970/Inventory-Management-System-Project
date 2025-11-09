================================================================================
                    MYSQL JDBC DRIVER - PUT JAR FILE HERE
================================================================================

This folder is for the MySQL JDBC Driver JAR file.

📁 WHAT SHOULD BE IN THIS FOLDER?
----------------------------------
After setup, this folder should contain:
  ✓ README.txt (this file)
  ✓ mysql-connector-j-X.X.X.jar (you need to download this)

Example:
  lib/
    ├── README.txt
    └── mysql-connector-j-9.5.0.jar  ← Download and place here

================================================================================
                    🔧 QUICK INSTALLATION (3 Steps)
================================================================================

STEP 1: Download
----------------
Go to: https://dev.mysql.com/downloads/connector/j/
- Select "Platform Independent"
- Download ZIP Archive
- Click "No thanks, just start my download"

STEP 2: Extract and Copy
-------------------------
- Extract the downloaded ZIP file
- Find: mysql-connector-j-X.X.X.jar
- Copy it to THIS folder (lib/)

STEP 3: Add to IntelliJ
------------------------
- Press: Ctrl+Alt+Shift+S (Project Structure)
- Modules → Dependencies → "+" → JARs or directories
- Select the JAR from this lib/ folder
- Click OK → Apply → OK

================================================================================
                    📖 NEED DETAILED INSTRUCTIONS?
================================================================================

For step-by-step guides with troubleshooting:

⚡ Fast Setup (15 min):
   → Read: ../01_QUICK_SETUP.txt (Section 1, Step 2)

📋 Complete Setup (30 min):
   → Read: ../02_COMPLETE_SETUP.txt (Section 3)

🆘 Having Errors?:
   → Read: ../01_QUICK_SETUP.txt (Section 2 - Troubleshooting)

================================================================================
                    ❓ WHY ISN'T THE JAR INCLUDED?
================================================================================

The MySQL Connector JAR is NOT in this repository because:
✓ It's a large binary file (~2-3 MB)
✓ Shouldn't be version controlled (Git best practice)
✓ Users should download the latest secure version
✓ Professional/industry standard approach

This teaches you proper dependency management - a valuable skill!

================================================================================
                    ✅ VERIFICATION
================================================================================

After adding the JAR, verify it worked:

1. Check this folder contains the JAR file
2. In IntelliJ: Ctrl+Alt+Shift+S → Modules → Dependencies
3. You should see "mysql-connector-j-X.X.X.jar" with a ✓
4. Run Main.java
5. Console should show: "Database created successfully"

If you see "JDBC Driver not found" error:
→ Read ../01_QUICK_SETUP.txt Section 2 (Troubleshooting)

================================================================================

                    Download the JAR and you're ready! 🚀

================================================================================
3. If you still see "ClassNotFoundException", restart IntelliJ IDEA

TROUBLESHOOTING:
----------------
Problem: "MySQL JDBC Driver not found!"
Solution:
  - Make sure the JAR is in the lib/ folder
  - Check that it's added in Project Structure → Modules → Dependencies
  - You should see "mysql-connector-j-X.X.X.jar" listed
  - Try: Build → Rebuild Project
  - Try: File → Invalidate Caches → Invalidate and Restart

Problem: JAR file shows in Libraries but still getting error
Solution:
  - Remove the old library reference
  - File → Project Structure → Modules → Dependencies
  - Find the old mysql-connector entry, select it, click "-" to remove
  - Add it again using the steps above
  - Make sure it's NOT pointing to a ZIP file or Downloads folder
  - It should point to: [YOUR_PROJECT]/lib/mysql-connector-j-X.X.X.jar

CURRENT COMPATIBLE VERSION:
---------------------------
This project was tested with: MySQL Connector/J 8.0.33 or newer
Recommended: MySQL Connector/J 9.5.0 (latest as of Nov 2025)

ALTERNATIVE: Include JAR in Repository (For School Projects)
-------------------------------------------------------------
If your instructor wants the JAR committed to the repository:
1. Copy the JAR to this lib/ folder
2. Edit .gitignore file
3. Comment out these lines (add # at the beginning):
   # mysql-connector-*.jar
   # mysql-connector-java-*.jar
   # lib/mysql-connector-*.jar
4. Run: git add lib/mysql-connector-j-X.X.X.jar
5. Run: git commit -m "Add MySQL JDBC driver for easy setup"
6. Run: git push

Now when others clone, they just need to add it in Project Structure.

================================================================================
Need Help? Check SETUP_GUIDE.txt in the project root for complete instructions
================================================================================

