@echo off
setlocal enabledelayedexpansion

REM PDFX Build Verification Script (Windows)
REM Validates that the project will build successfully on GitHub Actions

echo ================================================================================
echo.
echo                     PDFX Build Verification Script
echo.
echo ================================================================================
echo.

set CHECKS_PASSED=0
set CHECKS_FAILED=0
set WARNINGS=0

REM Check 1: Gradle wrapper exists
echo ================================================================================
echo   1. Checking Gradle Wrapper
echo ================================================================================
echo.

if exist "gradlew.bat" (
    echo [OK] gradlew.bat exists
    set /a CHECKS_PASSED+=1
) else (
    echo [ERROR] gradlew.bat not found
    set /a CHECKS_FAILED+=1
)

REM Check 2: Gradle wrapper properties
echo.
echo ================================================================================
echo   2. Validating Gradle Wrapper Properties
echo ================================================================================
echo.

if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo [OK] gradle-wrapper.properties exists
    set /a CHECKS_PASSED+=1
) else (
    echo [ERROR] gradle-wrapper.properties not found
    set /a CHECKS_FAILED+=1
)

REM Check 3: Java version
echo.
echo ================================================================================
echo   3. Checking Java Version
echo ================================================================================
echo.

java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java is installed
    set /a CHECKS_PASSED+=1
    java -version 2>&1 | findstr "version"
) else (
    echo [WARNING] Java not found in PATH
    set /a WARNINGS+=1
)

REM Check 4: Required build files
echo.
echo ================================================================================
echo   4. Checking Required Build Files
echo ================================================================================
echo.

set FILES=build.gradle.kts settings.gradle.kts gradle.properties app\build.gradle.kts app\src\main\AndroidManifest.xml

for %%f in (%FILES%) do (
    if exist "%%f" (
        echo [OK] %%f exists
        set /a CHECKS_PASSED+=1
    ) else (
        echo [ERROR] %%f is missing
        set /a CHECKS_FAILED+=1
    )
)

REM Check 5: Kotlin source files
echo.
echo ================================================================================
echo   5. Validating Kotlin Source Files
echo ================================================================================
echo.

set KOTLIN_COUNT=0
for /r "app\src\main\java" %%f in (*.kt) do set /a KOTLIN_COUNT+=1

if !KOTLIN_COUNT! gtr 0 (
    echo [OK] Found !KOTLIN_COUNT! Kotlin source files
    set /a CHECKS_PASSED+=1
) else (
    echo [ERROR] No Kotlin source files found
    set /a CHECKS_FAILED+=1
)

REM Check 6: Resource files
echo.
echo ================================================================================
echo   6. Checking Android Resources
echo ================================================================================
echo.

if exist "app\src\main\res" (
    echo [OK] res directory exists
    set /a CHECKS_PASSED+=1
) else (
    echo [ERROR] res directory not found
    set /a CHECKS_FAILED+=1
)

REM Check 7: GitHub workflows
echo.
echo ================================================================================
echo   7. Validating GitHub Workflows
echo ================================================================================
echo.

set WORKFLOWS=.github\workflows\debug-apk.yml .github\workflows\build-apk.yml .github\workflows\release-apk.yml

for %%w in (%WORKFLOWS%) do (
    if exist "%%w" (
        echo [OK] %%w exists
        set /a CHECKS_PASSED+=1
    ) else (
        echo [WARNING] %%w not found
        set /a WARNINGS+=1
    )
)

REM Check 8: .gitignore
echo.
echo ================================================================================
echo   8. Checking .gitignore
echo ================================================================================
echo.

if exist ".gitignore" (
    echo [OK] .gitignore exists
    set /a CHECKS_PASSED+=1
    
    findstr /C:"local.properties" .gitignore >nul
    if !errorlevel! equ 0 (
        echo [OK] .gitignore contains local.properties
        set /a CHECKS_PASSED+=1
    ) else (
        echo [WARNING] .gitignore missing local.properties
        set /a WARNINGS+=1
    )
    
    findstr /C:"*.jks" .gitignore >nul
    if !errorlevel! equ 0 (
        echo [OK] .gitignore contains *.jks
        set /a CHECKS_PASSED+=1
    ) else (
        echo [WARNING] .gitignore missing *.jks
        set /a WARNINGS+=1
    )
) else (
    echo [ERROR] .gitignore not found
    set /a CHECKS_FAILED+=1
)

REM Check 9: Sensitive files NOT in git
echo.
echo ================================================================================
echo   9. Security Check - Sensitive Files
echo ================================================================================
echo.

if exist "local.properties" (
    git ls-files --error-unmatch local.properties >nul 2>&1
    if !errorlevel! neq 0 (
        echo [OK] local.properties is not tracked by git
        set /a CHECKS_PASSED+=1
    ) else (
        echo [ERROR] SECURITY: local.properties is tracked by git!
        set /a CHECKS_FAILED+=1
    )
)

if exist "app\release-keystore.jks" (
    git ls-files --error-unmatch app\release-keystore.jks >nul 2>&1
    if !errorlevel! neq 0 (
        echo [OK] release-keystore.jks is not tracked by git
        set /a CHECKS_PASSED+=1
    ) else (
        echo [ERROR] SECURITY: release-keystore.jks is tracked by git!
        set /a CHECKS_FAILED+=1
    )
)

REM Check 10: Gradle sync
echo.
echo ================================================================================
echo   10. Testing Gradle Sync
echo ================================================================================
echo.

echo [INFO] Running gradle tasks (this may take a moment)...
call gradlew.bat tasks --no-daemon --quiet >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] Gradle sync successful
    set /a CHECKS_PASSED+=1
) else (
    echo [ERROR] Gradle sync failed
    set /a CHECKS_FAILED+=1
)

REM Summary
echo.
echo ================================================================================
echo   Verification Summary
echo ================================================================================
echo.
echo   [OK] Checks passed:  !CHECKS_PASSED!
echo   [ERROR] Checks failed:  !CHECKS_FAILED!
echo   [WARNING] Warnings:       !WARNINGS!
echo.

if !CHECKS_FAILED! equ 0 (
    echo ================================================================================
    echo.
    echo                        ALL CHECKS PASSED
    echo.
    echo            Your project is ready to build on GitHub Actions!
    echo.
    echo ================================================================================
    echo.
    echo Next steps:
    echo   1. git add .
    echo   2. git commit -m "Your message"
    echo   3. git push
    echo   4. Check GitHub Actions tab for build status
    echo.
    exit /b 0
) else (
    echo ================================================================================
    echo.
    echo                        VERIFICATION FAILED
    echo.
    echo                   Fix the issues above before pushing
    echo.
    echo ================================================================================
    echo.
    exit /b 1
)
