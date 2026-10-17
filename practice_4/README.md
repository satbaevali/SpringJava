# Практика 4: Слой персистентности (PostgreSQL, Spring Data JPA, HikariCP)

Проект по курсу **INFT3132: Backend Framework. Spring** (KBTU, Week 05).

---

## 📋 Чек-лист выполнения требований (Слайд 30)

| № | Требование из презентации | Реализация в проекте |
|---|---|---|
| 1 | **PostgreSQL DataSource + HikariCP** | Настроен в `application.yml` к базе `practice4_db`. Пароль через переменную `${DB_PASSWORD:}`. Пул: `maximum-pool-size: 10`, `connection-timeout: 3000ms` (fail-fast). |
| 2 | **Сущности (@Entity)** | `Category` и `Product`. Стратегия ID: `GenerationType.IDENTITY` (PostgreSQL). Связи строго `FetchType.LAZY`. Поле `@Version` для оптимистичной блокировки. |
| 3 | **Репозитории Spring Data JPA** | `CategoryRepository`, `ProductRepository`. Есть **Derived Query** (`findByCategoryId`, `findByName`) и **@Query** с `JOIN FETCH` (избегание N+1 проблемы) и `@Modifying` (атомарное списание). |
| 4 | **Пагинация и сортировка** | Эндпоинт `GET /api/products` с параметрами `Pageable`, аннотацией `@PageableDefault(size = 10, sort = "id")`. Лимит: `max-page-size: 100`. |
| 5 | **Транзакции (@Transactional)** | Методы сервисов размечены `@Transactional`. Методы чтения — `@Transactional(readOnly = true)`. Dirty Checking обновляет сущности без вызова `save()`. |
| 6 | **ddl-auto: validate + Flyway (Part 2)** | Схема полностью под контролем Flyway (`V1__init_schema.sql`), сидовые данные через `V2__seed_data.sql`. Hibernate работает в безопасном режиме `ddl-auto: validate`. |

---

## 🏗 Структура проекта

```text
src/main/resources/
├── db/migration/
│   ├── V1__init_schema.sql         # Flyway: DDL создание таблиц category и product
│   └── V2__seed_data.sql           # Flyway: DML наполнение начальными данными
├── application.yml                 # Общие настройки (Flyway, JPA validate, Pageable)
├── application-dev.yml             # Dev профиль: HikariCP пул (10), порт 8080
└── application-test.yml            # Test профиль: HikariCP пул (5), порт 8081

com.example.practice4/

├── dto/
│   ├── CategoryRequest.java        # DTO создания/обновления категории (@NotBlank, @Size)
│   ├── CategoryResponse.java       # DTO ответа категории (record)
│   ├── ProductRequest.java         # DTO создания товара (@Positive, @NotNull)
│   ├── ProductResponse.java        # DTO ответа товара с именем категории
│   └── ReduceStockRequest.java     # DTO списания остатка товара
├── exception/
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice с ProblemDetail (400, 404, 409, 500)
│   ├── InsufficientStockException.java # Ошибка нехватки остатка (409 Conflict)
│   ├── ResourceConflictException.java  # Конфликт дубликата (409 Conflict)
│   └── ResourceNotFoundException.java  # Не найдено (404 Not Found)
├── model/
│   ├── Category.java               # Сущность категории (@OneToMany LAZY)
│   └── Product.java                # Сущность товара (@ManyToOne LAZY, @Version)
├── repository/
│   ├── CategoryRepository.java     # Derived queries (findByName, existsByName)
│   └── ProductRepository.java      # Derived queries + @Query(JOIN FETCH) + @Modifying
├── service/
│   ├── CategoryService.java        # Бизнес-логика категорий (@Transactional)
│   └── ProductService.java         # Бизнес-логика товаров, списание остатка, пагинация
├── web/
│   ├── CategoryController.java     # REST API категорий (201 + Location, 204, 200)
│   └── ProductController.java      # REST API товаров с пагинацией и списанием остатка
└── Practice4Application.java       # Точка входа Spring Boot
```

---

## 🚀 Запуск и проверка

### 1. Запуск приложения:
```bash
./mvnw spring-boot:run
```

### 2. Запуск тестов:
```bash
./mvnw test
```
*Все 5 интеграционных тестов проверяют подключение к PostgreSQL, Derived queries, JOIN FETCH, пагинацию и списание остатков.*

### 3. Проверка через файл запросов:
В корне проекта лежит файл **`requests.http`** с 15 готовыми сценариями (CRUD, пагинация, фильтрация, проверка ошибок 400, 404, 409).
