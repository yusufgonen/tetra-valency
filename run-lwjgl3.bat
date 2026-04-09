@echo on
if /I "%~1" NEQ "child" (
  start "Tetra Valency" cmd /k call "%~f0" child
  exit /b
)
setlocal
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
set "LOG=%SCRIPT_DIR%saves\run-lwjgl3.log"
if not exist "saves" mkdir "saves"
if not exist "saves\options.json" echo {"musicVolume":0.7,"soundVolume":1.0,"fullscreen":false,"bindings":{}}>"saves\options.json"
echo ==== %date% %time% ==== >> "%LOG%"
call .\gradlew.bat --stop >> "%LOG%" 2>&1
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
powershell -NoProfile -Command "Remove-Item -Recurse -Force \"$env:USERPROFILE\\.gradle\\caches\\modules-2\\files-2.1\\org.lwjgl\\lwjgl\" -ErrorAction SilentlyContinue"
call .\gradlew.bat --refresh-dependencies :lwjgl3:run --stacktrace >> "%LOG%" 2>&1
echo.
echo Log saved to: "%LOG%"
echo Press any key to close this window.
pause >nul
endlocal
