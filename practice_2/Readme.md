## Practice 3 — conditional bean

`SmsNotificationSender` is registered only when `app.feature.mode=verbose`.

- Profile `dev` sets `app.feature.mode: verbose` → bean is created
- Profile `test` sets `app.feature.mode: silent` → bean is not created

To toggle manually:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev    # bean present
./mvnw spring-boot:run -Dspring-boot.run.profiles=test   # bean absent
```