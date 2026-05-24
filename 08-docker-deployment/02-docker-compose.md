# Docker Compose - app + postgres + redis

## Docker Compose nima?

Docker Compose = bir nechta container ni birgalikda boshqarish.

Masalan, ilova + malumotlar bazasi + redis ni bir vaqtda ishga tushirish.

## docker-compose.yml

```yaml
version: '3.8'

services:
  # 1. Malumotlar bazasi
  postgres:
    image: postgres:16
    container_name: myapp-db
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: myapp_user
      POSTGRES_PASSWORD: myapp_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - myapp-network

  # 2. Redis (keshlash uchun)
  redis:
    image: redis:7-alpine
    container_name: myapp-redis
    ports:
      - "6379:6379"
    networks:
      - myapp-network

  # 3. Ilova
  app:
    build: .
    container_name: myapp-app
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/myapp
      SPRING_DATASOURCE_USERNAME: myapp_user
      SPRING_DATASOURCE_PASSWORD: myapp_pass
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis
    networks:
      - myapp-network

networks:
  myapp-network:
    driver: bridge

volumes:
  postgres_data:
```

## Compose ni ishga tushirish

```bash
# Hamma servislarni ishga tushirish
docker-compose up -d

# Loglarni korish
docker-compose logs -f

# Servislarni to'xtatish
docker-compose down

# Servislarni to'xtatish va volume larni ochirish (DB tozalanadi)
docker-compose down -v
```

## Servislar orasidagi muloqot

Docker Compose da har bir servisga domain nomi orqali kirish mumkin:

```
app -> postgres:5432 (domain nomi = service nomi)
app -> redis:6379
```

Spring Boot konfiguratsiyasida:

```properties
# docker-compose da ishlatiladigan URL
spring.datasource.url=jdbc:postgresql://postgres:5432/myapp
spring.redis.host=redis
```

## Xulosa

- Docker Compose -> bir nechta container ni boshqaradi
- Service nomi = domain nomi (postgres, redis)
- depends_on -> qaysi container oldin ishga tushishi kerak
- volumes -> malumotlarni saglash
- networks -> container lar orasidagi muloqot
