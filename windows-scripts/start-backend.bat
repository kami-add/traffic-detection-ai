@echo off
setlocal
chcp 65001 >nul

echo =========================================
echo 启动后端 Spring Boot (8080)
echo =========================================

set "APP_DIR=D:\weburl\backend"

if not exist "%APP_DIR%\pom.xml" (
  echo [ERROR] 未找到 %APP_DIR%\pom.xml
  echo [TIP] 请先运行 copy-to-d-weburl.bat
  pause
  exit /b 1
)

cd /d "%APP_DIR%"

where mvn >nul 2>&1
if errorlevel 1 (
  echo [ERROR] 未找到 Maven(mvn)。请先安装 Maven 并加入 PATH。
  pause
  exit /b 1
)

echo [INFO] 正在启动后端...
call mvn spring-boot:run

pause
