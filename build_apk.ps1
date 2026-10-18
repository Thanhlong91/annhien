$currentDir = (Get-Item -LiteralPath ".").FullName
$env:JAVA_HOME = "C:\Users\taikh\android-tools\jdk-17.0.20.1+1"
$env:ANDROID_HOME = "C:\Users\taikh\android-tools\sdk"
$env:PATH = "$($env:JAVA_HOME)\bin;" + $env:PATH

# Remove any existing Z: subst
cmd /c "subst Z: /d >nul 2>&1"
cmd /c "subst Z: `"$currentDir`""
Set-Location "Z:\"

Write-Host "=== Đang biên dịch Android APK trên ổ đĩa Z: ==="
.\gradlew.bat assembleDebug --no-daemon

$exitCode = $LASTEXITCODE
cmd /c "subst Z: /d >nul 2>&1"
exit $exitCode
