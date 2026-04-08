@echo off
setlocal
cd /d "%~dp0"
if not exist "saves" mkdir "saves"
if not exist "saves\options.json" echo {"musicVolume":0.7,"soundVolume":1.0,"fullscreen":false,"bindings":{}}>"saves\options.json"
call .\gradlew.bat --stop
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
powershell -NoProfile -Command "Remove-Item -Recurse -Force \"$env:USERPROFILE\\.gradle\\caches\\modules-2\\files-2.1\\org.lwjgl\\lwjgl\" -ErrorAction SilentlyContinue"
call .\gradlew.bat --refresh-dependencies :lwjgl3:run
endlocal
