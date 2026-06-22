# Docker Compose — app + postgres + redis

Docker Compose bir nechta konteynerni birgalikda boshqaradi. Spring Boot ilova, PostgreSQL ma'lumotlar bazasi va Redis — barchasini bitta buyruq bilan ishga tushirish.

## docker-compose.yml

```yaml
services:
  # Ma'lumotlar bazasi
  postgres:
    image: postgres:16
    container_name: myapp-postgres
    environment:
      POSTGRES_DB: myapp_db
      POSTGRES_USER: myapp_user
      POSTGRES_PASSWORD: myapp_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql  # Boshlang'ich SQL
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myapp_user -d myapp_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - myapp-network

  # Redis (kesh va sessiya uchun)
  redis:
    image: redis:7-alpine
    container_name: myapp-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3
    networks:
      - myapp-network

  # Spring Boot ilova
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: myapp-app
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/myapp_db
      SPRING_DATASOURCE_USERNAME: myapp_user
      SPRING_DATASOURCE_PASSWORD: myapp_pass
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      postgres:
        condition: service_healthy  # Postgres tayyor bo'lgandan keyin
      redis:
        condition: service_healthy
    networks:
      - myapp-network
    restart: unless-stopped

networks:
  myapp-network:
    driver: bridge

volumes:
  postgres_data:
  redis_data:
```

`version:` maydoni eskirgan va Docker Compose v2+'da shart emas. Olib tashlansin.

## Ishga tushirish buyruqlari

```bash
# Barcha servislarni ishga tushirish
docker compose up -d

# Faqat infrastructure (DB va Redis) — ilova alohida
docker compose up -d postgres redis

# Loglarni ko'rish
docker compose logs -f
docker compose logs -f app  # Faqat ilova loglari

# Holat
docker compose ps

# To'xtatish (ma'lumotlar saqlanadi)
docker compose stop

# To'xtatish + konteynerlarni o'chirish
docker compose down

# To'xtatish + volume ham o'chirish (DB tozalanadi)
docker compose down -v
```

## Servislar orasidagi muloqot

Docker Compose'da har bir servisning nomi — uning domain nomi:

```
app → postgres:5432       (service nomi = hostname)
app → redis:6379

Tashqaridan:
localhost:5432 → postgres
localhost:6379 → redis
localhost:8080 → app
```

```properties
# application-prod.properties
# "localhost" emas, service nomi!
spring.datasource.url=jdbc:postgresql://postgres:5432/myapp_db
spring.data.redis.host=redis
```

## Development uchun alohida compose

```yaml
# docker-compose.dev.yml — faqat infrastructure
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: myapp_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_dev_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_dev_data:
```

```bash
# Dev'da: faqat DB va Redis Docker'da, ilova IDE'dan
docker compose -f docker-compose.dev.yml up -d

# Keyin ilova IntelliJ'dan ishga tushiriladi
# application-dev.properties:
# spring.datasource.url=jdbc:postgresql://localhost:5432/myapp_db
```

## Profile bilan ishlash

```yaml
# docker-compose.override.yml — local o'zgarishlar uchun (gitignore'da)
services:
  app:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      LOG_LEVEL: DEBUG
    volumes:
      - ./logs:/app/logs  # Loglarni host'ga chiqarish
```

```bash
docker compose up  # docker-compose.yml + docker-compose.override.yml birgalikda
```

## Foydali buyruqlar

```bash
# Konteyner ichiga kirish
docker compose exec postgres psql -U myapp_user -d myapp_db
docker compose exec app sh

# Konteyner ichida buyruq bajarish
docker compose exec app java -jar app.jar --version
docker compose exec postgres pg_dump -U myapp_user myapp_db > backup.sql

# Image'ni qayta build qilish
docker compose build app
docker compose up -d --build app

# Resurslarni ko'rish
docker compose stats
docker stats
```
