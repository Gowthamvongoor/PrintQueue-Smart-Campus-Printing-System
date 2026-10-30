@echo off
title PrintQueue - Smart Campus Printing System
setlocal enabledelayedexpansion

:: Check if Java is in PATH, otherwise use the openJdk-26 installation
where java >nul 2>nul
if %errorlevel% equ 0 (
    set "JAVA_CMD=java"
    set "JAVAC_CMD=javac"
) else (
    if exist "%USERPROFILE%\OneDrive\Desktop\Java\openJdk-26\bin\java.exe" (
        set "JAVA_CMD=%USERPROFILE%\OneDrive\Desktop\Java\openJdk-26\bin\java.exe"
        set "JAVAC_CMD=%USERPROFILE%\OneDrive\Desktop\Java\openJdk-26\bin\javac.exe"
    ) else (
        echo [ERROR] Java was not found in PATH or in the default openJdk-26 folder.
        echo Please ensure JDK is installed and configured.
        pause
        exit /b 1
    )
)

:: Compile sources into bin directory
if not exist "bin" mkdir bin
echo [INFO] Compiling PrintQueue Java sources...
"%JAVAC_CMD%" -d bin src\com\printqueue\model\*.java src\com\printqueue\util\*.java src\com\printqueue\service\*.java src\com\printqueue\Main.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)

echo [SUCCESS] Build successful. Launching PrintQueue...
echo.
"%JAVA_CMD%" -cp bin com.printqueue.Main

pause
