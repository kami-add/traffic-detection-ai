@echo off
setlocal
chcp 65001 >nul

echo =========================================
echo 一键启动: 后端 + 前端
echo =========================================

echo [INFO] 将在两个新窗口启动后端与前端...
start "LinkRisk Backend" cmd /k "D:\weburl\windows-scripts\start-backend.bat"
start "LinkRisk Frontend" cmd /k "D:\weburl\windows-scripts\start-frontend.bat"

echo [OK] 已启动。浏览器访问: http://localhost:5173
pause
