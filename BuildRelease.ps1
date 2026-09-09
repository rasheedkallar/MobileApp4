# BuildRelease.ps1

# Configure Java automatically

$JavaHome = "C:\Program Files\Android\Android Studio\jbr"

if (Test-Path $JavaHome) {
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$JavaHome\bin;$env:Path"
}

Write-Host "JAVA_HOME = $env:JAVA_HOME"


$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$OutputFolder = "C:\Users\rashe\source\repos\Abunaser\Bytes POS"

Write-Host "Building Android Release APK..." -ForegroundColor Green

Set-Location $ProjectRoot



$GradleFile = "$ProjectRoot\app\build.gradle"

$content = Get-Content $GradleFile -Raw

if ($content -match 'versionCode\s+(\d+)') {
    $newVersionCode = [int]$matches[1] + 1
    $content = $content -replace 'versionCode\s+\d+', "versionCode $newVersionCode"
}

if ($content -match 'versionName\s+"([\d\.]+)"') {
    $parts = $matches[1].Split('.')
    $last = [int]$parts[-1] + 1
    $parts[-1] = $last
    $newVersionName = $parts -join '.'

    $content = $content -replace 'versionName\s+"[\d\.]+"', "versionName `"$newVersionName`""
}

Set-Content $GradleFile $content

Write-Host "Updated Version:"
Write-Host "versionCode = $newVersionCode"
Write-Host "versionName = $newVersionName"




# Build Release APK
.\gradlew.bat clean assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed." -ForegroundColor Red
    exit 1
}

# Locate APK
$Apk = Get-ChildItem `
    -Path "$ProjectRoot\app\build\outputs\apk\release" `
    -Filter "*.apk" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $Apk) {
    Write-Host "APK not found." -ForegroundColor Red
    exit 1
}

# Create output folder
if (!(Test-Path $OutputFolder)) {
    New-Item -ItemType Directory -Path $OutputFolder -Force | Out-Null
}

# --------------------------------------------------
# Read Version Name / Version Code
# --------------------------------------------------

$GradleFile = "$ProjectRoot\app\build.gradle"

$VersionName = (
    Select-String `
        -Path $GradleFile `
        -Pattern 'versionName\s+"([^"]+)"'
).Matches.Groups[1].Value

$VersionCode = (
    Select-String `
        -Path $GradleFile `
        -Pattern 'versionCode\s+(\d+)'
).Matches.Groups[1].Value

if (:IsNullOrWhiteSpace($VersionName)) {
    $VersionName = "Unknown"
}

if (:IsNullOrWhiteSpace($VersionCode)) {
    $VersionCode = 0
}

# --------------------------------------------------
# Copy APK
# --------------------------------------------------

$TargetApk = Join-Path $OutputFolder "BytesPDA.apk"

Copy-Item $Apk.FullName $TargetApk -Force

# --------------------------------------------------
# Generate SHA256
# --------------------------------------------------

$SHA256 = (
    Get-FileHash `
        -Path $TargetApk `
        -Algorithm SHA256
).Hash.ToLower()

# --------------------------------------------------
# Get APK Size
# --------------------------------------------------

$FileSize = (Get-Item $TargetApk).Length

# --------------------------------------------------
# Build Version JSON
# --------------------------------------------------

$VersionInfo = @{
    fileSize    = $FileSize
    versionName = $VersionName
    sha256      = $SHA256
    versionCode = [int]$VersionCode
    platform    = "android"
    apkUrl      = "https://api.greenleafuae.com/Setup/BytesPDA.apk"
    notes       = "Bug fixes + performance improvements"
}

$JsonPath = Join-Path $OutputFolder "BytesPDAVersion.json"

$VersionInfo |
    ConvertTo-Json -Depth 10 |
    Set-Content $JsonPath -Encoding UTF8

# --------------------------------------------------
# Output
# --------------------------------------------------

Write-Host ""
Write-Host "====================================" -ForegroundColor Green
Write-Host "Build Completed Successfully" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host "APK     : $TargetApk"
Write-Host "JSON    : $JsonPath"
Write-Host "Version : $VersionName"
Write-Host "Code    : $VersionCode"
Write-Host "SHA256  : $SHA256"
Write-Host "Size    : $FileSize bytes"
Write-Host ""