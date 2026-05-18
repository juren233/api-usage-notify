param(
    [switch]$Clean
)

Set-Location "$PSScriptRoot\.."

if ($Clean) {
    Write-Host "[1/3] Cleaning previous build..." -ForegroundColor Cyan
    & .\gradlew.bat clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "CLEAN FAILED" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[1/3] Skipping clean for incremental build..." -ForegroundColor Cyan
}

Write-Host "[2/3] Building release APK..." -ForegroundColor Cyan
& .\gradlew.bat assembleRelease
if ($LASTEXITCODE -ne 0) {
    Write-Host "BUILD FAILED" -ForegroundColor Red
    exit 1
}

Write-Host "[3/3] Copying APK to script folder..." -ForegroundColor Cyan
$apkSrc = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $apkSrc) {
    Copy-Item $apkSrc "script\UsageNotify-release.apk" -Force
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  BUILD SUCCESS" -ForegroundColor Green
    Write-Host "  APK: script\UsageNotify-release.apk" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host "APK not found at $apkSrc" -ForegroundColor Red
    exit 1
}
