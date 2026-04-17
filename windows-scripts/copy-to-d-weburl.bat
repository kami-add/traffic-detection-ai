@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo =========================================
echo Link Risk 平台 - 复制到 D:\weburl
echo =========================================

set "SRC_DIR=%~dp0.."
for %%I in ("%SRC_DIR%") do set "SRC_DIR=%%~fI"
set "DEST_DIR=D:\weburl"

if not exist "D:\" (
  echo [ERROR] 未检测到 D 盘，请确认磁盘存在。
  pause
  exit /b 1
)

if not exist "%DEST_DIR%" (
  mkdir "%DEST_DIR%"
)

echo [INFO] 源目录: %SRC_DIR%
echo [INFO] 目标目录: %DEST_DIR%

echo [INFO] 正在复制（会自动覆盖旧文件）...
robocopy "%SRC_DIR%" "%DEST_DIR%" /E /R:1 /W:1 /XF "*.log" /XD ".git" "frontend\node_modules" "backend\target"
set "RC=%ERRORLEVEL%"

if %RC% GEQ 8 (
  echo [ERROR] 复制失败，robocopy 返回码: %RC%
  pause
  exit /b %RC%
)

echo [OK] 已复制到 %DEST_DIR%
echo [TIP] 下一步可在 D:\weburl 执行 windows-scripts\start-all.bat
pause
