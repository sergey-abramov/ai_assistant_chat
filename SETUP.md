# 🚀 Быстрый старт AI Telegram Bot

## Пошаговая настройка

### 1. Создание Telegram бота
1. Откройте Telegram
2. Найдите и напишите [@BotFather](https://t.me/BotFather)
3. Отправьте команду `/newbot`
4. Следуйте инструкциям:
   - Введите имя бота (например: "My AI Assistant")
   - Введите username бота (должен заканчиваться на "bot", например: "my_ai_assistant_bot")
5. Сохраните полученный **token** (что-то вроде: `123456789:ABCDEF...`)
6. Сохраните **username** бота

### 2. Получение Groq API ключа (БЕСПЛАТНО)
1. Зайдите на [console.groq.com](https://console.groq.com/)
2. Зарегистрируйтесь с помощью Google/GitHub или email
3. После входа перейдите в раздел "API Keys"
4. Нажмите "Create API Key"
5. Дайте ключу название (например: "Telegram Bot")
6. Скопируйте и сохраните полученный ключ (начинается с `gsk_...`)

### 3. Настройка переменных окружения

#### Windows (PowerShell):
```powershell
$env:TELEGRAM_BOT_TOKEN="your_telegram_bot_token_here"
$env:TELEGRAM_BOT_USERNAME="your_bot_username_here"
$env:AI_API_KEY="your_groq_api_key_here"
```

#### Windows (Command Prompt):
```cmd
set TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
set TELEGRAM_BOT_USERNAME=your_bot_username_here
set AI_API_KEY=your_groq_api_key_here
```

### 4. Запуск

#### Вариант 1: Автоматический запуск
Дважды кликните на файл `run.bat` - он автоматически проверит все зависимости и запустит бота.

#### Вариант 2: Ручной запуск
```bash
java -jar target/telegram-ai-bot-1.0.0.jar
```

### 5. Проверка работы
1. Найдите своего бота в Telegram по username
2. Нажмите "Start" или отправьте `/start`
3. Отправьте любое сообщение боту
4. Получите ответ от AI!

## 📋 Команды бота
- `/start` - Начать работу с ботом
- `/help` - Показать справку
- `/status` - Показать статус бота и пользователя

## ⚙️ Дополнительные настройки

### Изменение AI модели
```bash
set AI_MODEL=llama-3.1-70b-versatile  # Более мощная модель
```

### Изменение лимитов
```bash
set BOT_RATE_LIMIT=20  # 20 запросов в минуту
set AI_MAX_TOKENS=2000  # Более длинные ответы
```

### Использование других AI провайдеров

#### OpenAI:
```bash
set AI_PROVIDER_TYPE=openai
set AI_API_URL=https://api.openai.com/v1/chat/completions
set AI_MODEL=gpt-3.5-turbo
set AI_API_KEY=your_openai_api_key
```

## 🔧 Устранение проблем

### Бот не отвечает
- Проверьте правильность токена
- Убедитесь, что бот не заблокирован в Telegram
- Проверьте логи приложения

### AI не работает
- Проверьте правильность API ключа Groq
- Убедитесь в наличии интернет-соединения
- Проверьте квоты на api.groq.com

### Ошибки запуска
- Убедитесь, что установлена Java 19+
- Проверьте, что все переменные окружения установлены

## 📊 Мониторинг
- Отправьте `/status` боту для проверки состояния
- Логи выводятся в консоль
- Рекорды пользователей сохраняются в файл

---

**Готово! Ваш AI бот работает! 🎉**
