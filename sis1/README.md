# SIS 1: REST API для онлайн-магазина (Online Store)

Проект по курсу **Spring Framework / Java Backend**. 
Реализация REST API для управления каталогом товаров, категориями и заказами в соответствии со всеми 6 пунктами чек-листа СРС 1.

---

## 📋 Чек-лист реализации

| № | Пункт требования | Реализация в проекте |
|---|---|---|
| 1 | **CRUD эндпоинты с DTO** | Реализованы для `Category`, `Product`, `Order` без утечки внутренних сущностей. Входящие и исходящие DTO разделены. |
| 2 | **Валидация (Bean Validation)** | Аннотации `@Valid`, `@NotBlank`, `@Size`, `@Positive`, `@PositiveOrZero`, `@Email`, `@Pattern(SKU)`, каскадная валидация `List<@Valid OrderItemRequest>`. |
| 3 | **Централизованная обработка ошибок** | `@RestControllerAdvice` в классе `GlobalExceptionHandler`. Единый JSON-формат ошибки (`ApiErrorResponse`: timestamp, status, error, message, path, fieldErrors). |
| 4 | **Корректные HTTP-статусы** | `201 Created` с заголовком `Location` (через `ServletUriComponentsBuilder`), `204 No Content` при удалении, `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict`. |
| 5 | **Maven профили и настройки** | Профили `dev` (порт 8080) и `test` (порт 8081) в `pom.xml`, `application-dev.properties` и `application-test.properties`, `@ConfigurationProperties(prefix = "app")`, `StartupLogger`. |
| 6 | **HTTP клиент / Коллекция запросов** | Файл `requests.http` с 28 сценариями (успешные CRUD операции, валидационные ошибки 400, несуществующие ID 404, бизнес-конфликты 409). |

---

## 🏗 Архитектура проекта

```text
sis1/
├── src/main/java/com/example/sis1/
│   ├── config/            # Конфигурация приложения и логирование профилей (AppProperties, StartupLogger)
│   ├── dto/               # Объекты передачи данных и валидация (Request / Response DTO)
│   ├── exception/         # Исключения и глобальный перехватчик (@RestControllerAdvice)
│   ├── model/             # Доменные модели (Category, Product, Order, OrderItem, OrderStatus)
│   ├── repository/        # Слой доступа к данным (InMemory на ConcurrentHashMap + AtomicLong)
│   ├── service/           # Бизнес-логика (проверка остатков, конфликтов и уникальности)
│   ├── web/               # REST-контроллеры (CategoryController, ProductController, OrderController, ConfigController)
│   └── Sis1Application.java # Главный класс запуска Spring Boot
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-test.properties
├── requests.http          # Коллекция HTTP-запросов для проверки API
├── SIS1_Defense_Guide.html # Подробная шпаргалка к защите (с ответами на вопросы преподавателя)
├── SIS1_Defense_Guide.txt  # Текстовая версия шпаргалки
└── pom.xml                # Конфигурация сборки и профили Maven
```

---

## 🚀 Запуск и тестирование

### Запуск в профиле DEV (порт 8080):
```bash
./mvnw spring-boot:run -Pdev
```
или без флага (dev активен по умолчанию):
```bash
./mvnw spring-boot:run
```

### Запуск в профиле TEST (порт 8081):
```bash
./mvnw spring-boot:run -Ptest
```

### Запуск автоматических тестов:
```bash
./mvnw test
```

---

## 📡 Основные эндпоинты API

### Категории (`/api/categories`)
- `GET /api/categories` — список всех категорий
- `GET /api/categories/{id}` — получение категории по ID (404 если не найдена)
- `POST /api/categories` — создание категории (`201 Created` + заголовок `Location`)
- `PUT /api/categories/{id}` — обновление категории
- `DELETE /api/categories/{id}` — удаление категории (`204 No Content`, 409 если есть связанные товары)

### Товары (`/api/products`)
- `GET /api/products` — список товаров (фильтрация по `?categoryId=...`)
- `GET /api/products/{id}` — товар по ID
- `POST /api/products` — создание товара (`201 Created` + `Location`, валидация SKU по regex `^[A-Z0-9-]{3,20}$`)
- `PUT /api/products/{id}` — обновление товара
- `DELETE /api/products/{id}` — удаление товара (`204 No Content`)

### Заказы (`/api/orders`)
- `GET /api/orders` — список заказов
- `GET /api/orders/{id}` — заказ по ID со списком позиций
- `POST /api/orders` — создание заказа (проверка наличия товара, списание остатка склада)
- `PATCH /api/orders/{id}/status` — смена статуса (`NEW -> PAID -> SHIPPED -> COMPLETED / CANCELLED`)

### Профиль и конфигурация (`/api/config`)
- `GET /api/config` — информация о текущем активном профиле, названии магазина и валюте
