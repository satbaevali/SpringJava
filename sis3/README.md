## SIS 3 — documented API and events

Start Kafka locally (KRaft mode on port 9092):
```bash
docker compose up -d
```

Run the application:
```bash
./mvnw spring-boot:run
```

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

Run tests and JaCoCo report:
```bash
./mvnw verify
```
