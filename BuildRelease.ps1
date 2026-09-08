# BuildRelease.ps1

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$OutputFolder = "C:\Users\rashe\source\repos\Abunaser\Bytes POS"

Write-Host "Building Android Release APK..." -ForegroundColor Green

Set-Location $ProjectRoot

# Clean + Build Release APK
.\gradlew.bat clean assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed." -ForegroundColor Red
    exit 1
}

# Find generated APK
$Apk = Get-ChildItem `
    -Path "$ProjectRoot\app\build\outputs\apk\release" `
    -Filter "*.apk" `
    | Sort-Object LastWriteTime -Descending `
    | Select-Object -First 1

if ($null -eq $Apk) {
    Write-Host "APK not found." -ForegroundColor Red
    exit 1
}

# Create destination folder if missing
if (!(Test-Path $OutputFolder)) {
    New-Item -ItemType Directory -Path $OutputFolder -Force | Out-Null
}

$TimeStamp = Get-Date -Format "yyyyMMdd_HHmmss"

$TargetFile = Join-Path `
    $OutputFolder `
    ("BytesPOS_" + $TimeStamp + ".apk")

Copy-Item $Apk.FullName $TargetFile -Force

Write-Host ""
Write-Host "APK Generated Successfully" -ForegroundColor Green
Write-Host "Source : $($Apk.FullName)"
Write-Host "Output : $TargetFile"
