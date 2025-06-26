@echo off
echo ====================================
echo AI Assistant Telegram Bot Launcher
echo ====================================
echo.

REM Проверка Java
echo [1/4] Проверка Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java не найдена! Установите Java 19+ с oracle.com или adoptium.net
    pause
    exit /b 1
)

java -version 2>&1 | findstr "version" | findstr /r "\"1[89]\." >nul 2>&1
if %errorlevel% neq 0 (
    java -version 2>&1 | findstr "version" | findstr /r "\"2[0-9]\." >nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ Требуется Java 19+. Текущая версия:
        java -version
        pause
        exit /b 1
    )
)
echo ✅ Java найдена

REM Проверка Maven
echo [2/4] Проверка Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven не найден! Установите Maven с maven.apache.org
    pause
    exit /b 1
)
echo ✅ Maven найден

REM Проверка переменных окружения
echo [3/4] Проверка переменных окружения...
if "%TELEGRAM_BOT_TOKEN%"=="YOUR_BOT_TOKEN_HERE" (
    echo ❌ Не настроен TELEGRAM_BOT_TOKEN
    echo Получите токен от @BotFather в Telegram
    echo Затем выполните: set TELEGRAM_BOT_TOKEN=your_actual_token
    pause
    exit /b 1
)
if "%TELEGRAM_BOT_TOKEN%"=="" (
    echo ❌ Не настроен TELEGRAM_BOT_TOKEN
    echo Получите токен от @BotFather в Telegram
    echo Затем выполните: set TELEGRAM_BOT_TOKEN=your_actual_token
    pause
    exit /b 1
)

if "%AI_API_KEY%"=="YOUR_GROQ_API_KEY_HERE" (
    echo ❌ Не настроен AI_API_KEY
    echo Получите ключ на console.groq.com
    echo Затем выполните: set AI_API_KEY=your_actual_key
    pause
    exit /b 1
)
if "%AI_API_KEY%"=="" (
    echo ❌ Не настроен AI_API_KEY
    echo Получите ключ на console.groq.com
    echo Затем выполните: set AI_API_KEY=your_actual_key
    pause
    exit /b 1
)
echo ✅ Переменные окружения настроены

REM Сборка и запуск
echo [4/4] Сборка и запуск приложения...
echo Выполняется mvn clean package...
mvn clean package -DskipTests >build.log 2>&1
if %errorlevel% neq 0 (
    echo ❌ Ошибка сборки! Проверьте build.log
    pause
    exit /b 1
)
echo ✅ Сборка завершена успешно

echo.
echo 🤖 Запуск AI Assistant Telegram Bot...
echo Для остановки нажмите Ctrl+C
echo.

java -jar target/telegram-ai-bot-1.0.0.jar

echo.
echo Приложение завершено.
pause
