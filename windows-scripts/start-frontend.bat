@echo off
setlocal
chcp 65001 >nul

echo =========================================
echo 启动前端 Vue (5173)
echo =========================================

set "APP_DIR=D:\weburl\frontend"

if not exist "%APP_DIR%\package.json" (
  echo [ERROR] 未找到 %APP_DIR%\package.json
  echo [TIP] 请先运行 copy-to-d-weburl.bat
  pause
  exit /b 1
)

cd /d "%APP_DIR%"

where npm >nul 2>&1
if errorlevel 1 (
  echo [ERROR] 未找到 npm。请先安装 Node.js LTS。
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo [INFO] 首次启动，正在安装前端依赖...
  call npm install
  if errorlevel 1 (
    echo [ERROR] npm install 失败
    pause
    exit /b 1
  )
)

echo [INFO] 正在启动前端开发服务...
call npm run dev

pause
