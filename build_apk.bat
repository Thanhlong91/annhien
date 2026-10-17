@echo off
chcp 65001 > nul
set JAVA_HOME=C:\Users\taikh\android-tools\jdk-17.0.20.1+1
set ANDROID_HOME=C:\Users\taikh\android-tools\sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%PATH%
echo DIR is: "%~dp0"
"%JAVA_HOME%\bin\java.exe" -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain assembleDebug --stacktrace
if %ERRORLEVEL% EQU 0 (
    copy /y "%~dp0app\build\outputs\apk\debug\app-debug.apk" "%~dp0app-debug.apk"
    echo.
    echo ========================================================
    echo BIEN DICH THANH CONG! File APK da duoc copy ra app-debug.apk
    echo ========================================================
)
pause
