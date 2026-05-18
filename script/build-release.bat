@echo off
setlocal

cd /d "%~dp0.."

if /I "%~1"=="/clean" (
    echo [1/3] Cleaning previous build...
    call gradlew.bat clean
    if %errorlevel% neq 0 (
        echo CLEAN FAILED
        pause
        exit /b 1
    )
) else (
    echo [1/3] Skipping clean for incremental build...
)

echo [2/3] Building release APK...
call gradlew.bat assembleRelease
if %errorlevel% neq 0 (
    echo BUILD FAILED
    pause
    exit /b 1
)

echo [3/3] Copying APK to script folder...
set "APK_SRC=app\build\outputs\apk\release\app-release.apk"
if exist "%APK_SRC%" (
    copy /Y "%APK_SRC%" "script\UsageNotify-release.apk"
    echo.
    echo ========================================
    echo   BUILD SUCCESS
    echo   APK: script\UsageNotify-release.apk
    echo ========================================
) else (
    echo APK not found at %APK_SRC%
    pause
    exit /b 1
)

pause
