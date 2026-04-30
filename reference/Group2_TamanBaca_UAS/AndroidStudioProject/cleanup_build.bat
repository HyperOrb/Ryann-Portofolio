@echo off
REM Batch script untuk clean gradle build di Windows
REM Run ini sebagai Administrator untuk hasil terbaik

echo.
echo ========================================
echo   PojokBaca - Clean Build Script
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo WARNING: Run this as Administrator for best results
    echo.
)

echo 1. Stopping Gradle daemon...
gradlew --stop

echo 2. Deleting .gradle folder...
if exist ".gradle" (
    rmdir /s /q .gradle
    echo   ✓ .gradle deleted
) else (
    echo   ℹ .gradle not found (already deleted?)
)

echo 3. Deleting .idea folder...
if exist ".idea" (
    rmdir /s /q .idea
    echo   ✓ .idea deleted
) else (
    echo   ℹ .idea not found (already deleted?)
)

echo 4. Deleting app/build folder...
if exist "app\build" (
    rmdir /s /q app\build
    echo   ✓ app/build deleted
) else (
    echo   ℹ app/build not found (already deleted?)
)

echo 5. Deleting build folder...
if exist "build" (
    rmdir /s /q build
    echo   ✓ build deleted
) else (
    echo   ℹ build not found (already deleted?)
)

echo.
echo ========================================
echo   ✓ Clean complete!
echo ========================================
echo.
echo NEXT STEPS:
echo   1. Close Android Studio (if open)
echo   2. Reopen Android Studio
echo   3. Wait for Gradle sync to complete
echo   4. Build → Rebuild Project
echo.
pause

