# 🤖 AI Assistant Telegram Bot

Умный Telegram бот с интеграцией языковых моделей, построенный на Java 19+ с использованием Spring Boot и принципов SOLID.

![Java](https://img.shields.io/badge/Java-19%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![Telegram](https://img.shields.io/badge/Telegram-Bot%20API-blue)
![AI](https://img.shields.io/badge/AI-Groq%20%7C%20OpenAI-purple)

## 📋 Описание

Этот проект представляет собой полнофункциональный Telegram бот, который интегрируется с различными AI провайдерами для генерации ответов на вопросы пользователей. Бот поддерживает:

- 🔄 Асинхронную обработку запросов
- ⚡ Rate limiting для контроля нагрузки
- 👥 Управление пользователями
- 🔧 Гибкую конфигурацию через environment variables
- 📊 Мониторинг статуса и здоровья
- 🛡️ Обработку ошибок и валидацию

## 🏗️ Архитектура

Проект следует принципам SOLID и использует современные практики разработки:

### Принципы SOLID
- **Single Responsibility**: Каждый класс отвечает за одну конкретную задачу
- **Open/Closed**: Легко добавлять новых AI провайдеров без изменения существующего кода
- **Liskov Substitution**: Интерфейсы позволяют заменять реализации
- **Interface Segregation**: Специализированные интерфейсы для разных типов операций
- **Dependency Inversion**: Зависимости инжектируются через конструкторы

### Структура проекта
```
src/main/java/com/aiassistant/
├── AiAssistantApplication.java     # Главный класс приложения
├── bot/
│   └── AiAssistantBot.java         # Основная логика Telegram бота
├── config/
│   ├── ApplicationProperties.java   # Конфигурация приложения
│   └── HttpClientConfig.java       # Настройка HTTP клиента
├── exception/
│   ├── AiServiceException.java     # Базовое исключение AI сервиса
│   └── RateLimitExceededException.java # Исключение превышения лимита
├── model/
│   ├── AiRequest.java              # Модель запроса к AI
│   ├── AiResponse.java             # Модель ответа от AI
│   └── TelegramUser.java           # Модель пользователя
├── service/
│   ├── AiService.java              # Интерфейс AI сервиса
│   ├── UserService.java            # Интерфейс управления пользователями
│   ├── RateLimitService.java       # Интерфейс rate limiting
│   └── impl/
│       ├── GroqAiService.java      # Реализация для Groq API
│       ├── InMemoryUserService.java # In-memory хранение пользователей
│       └── InMemoryRateLimitService.java # In-memory rate limiting
└── util/                           # Утилиты (если потребуются)
```

## 🛠️ Требования

### Системные требования
- **Java 19+** (рекомендуется Java 21)
- **Maven 3.6+**
- **Доступ к интернету** для API вызовов

### Внешние сервисы
- **Telegram Bot Token** (получить от [@BotFather](https://t.me/BotFather))
- **AI API Key** (по умолчанию Groq - бесплатный)

## 📦 Установка и настройка

### 1. Клонирование проекта
```bash
git clone <repository-url>
cd ai_assistant_chat
```

### 2. Создание Telegram бота
1. Напишите [@BotFather](https://t.me/BotFather) в Telegram
2. Выполните команду `/newbot`
3. Следуйте инструкциям для создания бота
4. Сохраните полученный **token** и **username**

### 3. Получение AI API ключа (Groq - бесплатно)
1. Зайдите на [console.groq.com](https://console.groq.com/)
2. Зарегистрируйтесь или войдите
3. Перейдите в раздел "API Keys"
4. Создайте новый API ключ
5. Сохраните полученный ключ

### 4. Настройка application.yml

Вместо использования переменных окружения, настройки теперь хранятся в `src/main/resources/application.yml`.

1. Откройте файл `application.yml`.
2. Замените значения `YOUR_BOT_TOKEN_HERE`, `YOUR_BOT_USERNAME_HERE` и `YOUR_GROQ_API_KEY_HERE` на ваши реальные значения.

Пример:
```yaml
telegram-bot-token: "ваш_токен_бота"
telegram-bot-username: "ваш_юзернейм_бота"
ai-provider:
  api-key: "ваш_api_ключ"
```
### 5. Сборка и запуск

#### Сборка проекта:
```bash
mvn clean package
```

#### Запуск:
```bash
mvn spring-boot:run
```

#### Или запуск JAR файла:
```bash
java -jar target/telegram-ai-bot-1.0.0.jar
```

#### Для Windows (удобный запуск):
```cmd
run.bat
```

## 🎮 Использование

### Команды бота
- `/start` - Начать работу с ботом
- `/help` - Показать справку
- `/status` - Показать статус пользователя и бота

### Базовое использование
1. Найдите своего бота в Telegram по username
2. Нажмите "Start" или отправьте `/start`
3. Задайте любой вопрос боту
4. Получите ответ от AI

### Примеры вопросов
- "Объясни, что такое Java"
- "Напиши простую функцию сортировки"
- "Какая погода в Москве?" (бот попросит уточнить, так как не имеет доступа к реальным данным)

## ⚙️ Конфигурация

### Основные настройки
Все настройки настраиваются в файле `src/main/resources/application.yml`:

| Параметр | Путь в YAML | Значение по умолчанию | Описание |
|----------|-------------|----------------------|----------|
| AI Провайдер | `ai-provider.type` | `groq` | Тип AI провайдера |
| AI Модель | `ai-provider.model` | `llama-3.1-8b-instant` | Модель для генерации |
| Макс. токенов | `ai-provider.max-tokens` | `1000` | Максимум токенов в ответе |
| Температура | `ai-provider.temperature` | `0.7` | Креативность (0.0-1.0) |
| Rate Limit | `bot-behavior.rate-limit-per-minute` | `10` | Запросов в минуту на пользователя |
| Макс. длина | `bot-behavior.max-message-length` | `4000` | Максимальная длина сообщения |

### Альтернативные AI провайдеры

#### OpenAI:
```yaml
ai-provider:
  type: "openai"
  api-url: "https://api.openai.com/v1/chat/completions"
  model: "gpt-3.5-turbo"
  api-key: "your_openai_api_key"
```

#### Локальная модель (Ollama):
```yaml
ai-provider:
  type: "ollama"
  api-url: "http://localhost:11434/v1/chat/completions"
  model: "llama2"
  # api-key не требуется для локальных моделей
```

## 🧪 Тестирование

### Запуск тестов:
```bash
mvn test
```

### Ручное тестирование:
1. Запустите приложение
2. Отправьте боту `/status` для проверки работоспособности
3. Задайте простой вопрос для проверки AI интеграции

## 🚀 Развертывание

### Docker (если требуется)
Создайте `Dockerfile`:
```dockerfile
FROM openjdk:19-jdk-slim
COPY target/telegram-ai-bot-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Heroku
1. Установите Heroku CLI
2. Создайте приложение: `heroku create your-bot-name`
3. Настройте переменные окружения в Heroku Dashboard
4. Деплой: `git push heroku main`

### VPS/Dedicated Server
1. Установите Java 19+
2. Скопируйте JAR файл на сервер
3. Создайте systemd service для автозапуска
4. Настройте переменные окружения

## 🔧 Расширение функциональности

### Добавление нового AI провайдера
1. Создайте новый класс, реализующий `AiService`
2. Добавьте `@ConditionalOnProperty` для активации
3. Обновите конфигурацию в `application.yml`

### Добавление базы данных
1. Замените `InMemoryUserService` на JPA реализацию
2. Добавьте зависимости Spring Data JPA и драйвер БД
3. Создайте Entity классы и Repository интерфейсы

### Добавление новых команд
1. Расширьте метод `handleCommand` в `AiAssistantBot`
2. Добавьте новые case в switch statement
3. Обновите help message

## 📊 Мониторинг

### Health Check
Приложение предоставляет health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

### Логи
Логи настроены на уровне DEBUG для основного пакета. Основные события:
- Создание новых пользователей
- Обработка сообщений
- Ошибки AI API
- Rate limiting события

## 🐛 Устранение неполадок

### Бот не отвечает
1. Проверьте логи на ошибки
2. Убедитесь, что токен и username правильные
3. Проверьте доступность AI API

### AI не работает
1. Проверьте валидность API ключа
2. Убедитесь в доступности AI сервиса
3. Проверьте квоты и лимиты провайдера

### Ошибки сборки
1. Убедитесь в правильной версии Java (19+)
2. Проверьте доступность Maven Central
3. Очистите локальный Maven репозиторий: `mvn dependency:purge-local-repository`

## 📈 Производительность

### Рекомендации
- **Rate Limiting**: По умолчанию 10 запросов в минуту на пользователя
- **Асинхронность**: Все AI запросы обрабатываются асинхронно
- **Таймауты**: 30 секунд на AI запрос по умолчанию
- **Memory**: In-memory хранение подходит для небольших нагрузок

### Масштабирование
- Для продакшена замените in-memory сервисы на Redis/Database
- Добавьте метрики (Micrometer + Prometheus)
- Используйте connection pooling для HTTP клиента

## 🤝 Вклад в разработку

1. Форкните репозиторий
2. Создайте feature branch
3. Следуйте принципам SOLID
4. Добавьте тесты для новой функциональности
5. Создайте Pull Request

## 📄 Лицензия

Этот проект создан в образовательных целях и может быть свободно использован и модифицирован.

## 🆘 Поддержка

Если у вас возникли вопросы или проблемы:
1. Проверьте раздел "Устранение неполадок"
2. Изучите логи приложения
3. Создайте Issue в репозитории

---

**Удачного использования! 🚀🤖**

# ai_assistant_chat