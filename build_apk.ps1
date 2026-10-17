$env:JAVA_HOME = "C:\Users\taikh\android-tools\jdk-17.0.20.1+1"
$env:ANDROID_HOME = "C:\Users\taikh\android-tools\sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  DANG TIEN HANH BIEN DICH APK (AN NHIEN) TREN MAY TINH" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

& "$env:JAVA_HOME\bin\java.exe" "-Dorg.gradle.appname=gradlew" -classpath "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain assembleDebug --stacktrace

if ($LASTEXITCODE -eq 0) {
    Copy-Item "app\build\outputs\apk\debug\app-debug.apk" -Destination "app-debug.apk" -Force
    Write-Host "`n>>> BIEN DICH THANH CONG! File APK da duoc copy ra thu muc goc: $(Get-Location)\app-debug.apk" -ForegroundColor Green
} else {
    Write-Host "`n>>> CO LOI XAY RA KHI BIEN DICH!" -ForegroundColor Red
}
