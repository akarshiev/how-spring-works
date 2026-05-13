# Connection Pool - HikariCP

## Connection Pool nima?

Connection Pool = malumotlar bazasiga ulanishlarni saglaydigan "hovuz".

Har safar malumotlar bazasiga so'rov yuborganda, yangi ulanish ochish kerak:

```
1. TCP ulanish ochish (1-2ms)
2. Autentifikatsiya (1-2ms)
3. So'rov yuborish
4. Javob olish
5. Ulanishni yopish
```

Har safar yangi ulanish ochish juda sekin. Connection Pool bu muammoni yechadi:

```
1. Ilova ishga tushganda 10 ta ulanish ochiladi
2. Ulanishlar "hovuz"da saqlanadi
3. So'rov kelganda, tayyor ulanish beriladi
4. So'rov tugaganda, ulanish yopilmaydi, hovuzga qaytariladi
```

## HikariCP

Spring Boot default connection pool = HikariCP. Bu eng tez connection pool.

HikariCP ni qoshish shart emas, spring-boot-starter-data-jpa bilan oz-ozidan keladi.

## HikariCP sozlamalari

```properties
# application.properties
spring.datasource.hikari.connection-timeout=30000       # 30 soniya kutish
spring.datasource.hikari.maximum-pool-size=10            # Eng kop 10 ta ulanish
spring.datasource.hikari.minimum-idle=5                  # Eng kam 5 ta bo'sh ulanish
spring.datasource.hikari.max-lifetime=1800000            # 30 daqiqa (1800000ms)
spring.datasource.hikari.idle-timeout=600000             # 10 daqiqa (600000ms)
spring.datasource.hikari.pool-name=MyPool               # Pool nomi (log da korinadi)
```

## Parametrlarni tushuntirish

| Parametr | Default | Ma'nosi |
|----------|---------|---------|
| maximum-pool-size | 10 | Eng kop 10 ta ulanish |
| minimum-idle | 10 | Eng kam 5 ta bo'sh ulanish |
| connection-timeout | 30000 | Ulanish uchun kutish vaqti (ms) |
| idle-timeout | 600000 | Bo'sh ulanishni ochirish vaqti (ms) |
| max-lifetime | 1800000 | Ulanishni maksimal ishlatish vaqti (ms) |

## Qancha ulanish kerak?

Quyidagi formuladan foydalaning:

```
connections = (core_count * 2) + effective_spindle_count
```

Oddiy qilib:

| Server | Tavsiya |
|--------|---------|
| 2 yadro | 10 ta ulanish |
| 4 yadro | 15-20 ta ulanish |
| 8 yadro | 30-40 ta ulanish |

**Ko'p ulanish har doim yaxshi emas!** Kop ulanish = kop resurs.

## Pool ni kuzatish

```properties
# application.properties
spring.datasource.hikari.pool-name=MyAppPool
logging.level.com.zaxxer.hikari=DEBUG  # Pool loglarini korish
```

Log:

```
MyAppPool - Starting...
MyAppPool - Start completed.
MyAppPool - Pool stats (total=5, active=0, idle=5, waiting=0)
MyAppPool - Pool stats (total=5, active=2, idle=3, waiting=0)
```

## Kod orqali sozlash

```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("postgres");
        config.setPassword("123");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("MyAppPool");
        
        // Qoshimcha sozlamalar
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }
}
```

## Connection Pool ni tekshirish

```java
@Component
public class PoolMonitor {
    
    private final HikariDataSource dataSource;
    
    public PoolMonitor(DataSource dataSource) {
        this.dataSource = (HikariDataSource) dataSource;
    }
    
    @Scheduled(fixedRate = 60000)  // Har daqiqada
    public void logPoolStats() {
        System.out.println("=== Pool Stats ===");
        System.out.println("Total: " + dataSource.getHikariPoolMXBean().getTotalConnections());
        System.out.println("Active: " + dataSource.getHikariPoolMXBean().getActiveConnections());
        System.out.println("Idle: " + dataSource.getHikariPoolMXBean().getIdleConnections());
        System.out.println("Waiting: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}
```

## Xulosa

- Connection Pool = tayyor ulanishlar hovuzi
- Har safar yangi ulanish ochish sekin -> Pool tez
- HikariCP = Spring Boot default (eng tez)
- maksimum-pool-size -> 10-30 (serverga qarab)
- Pool ni kuzatib turing (HikariPoolMXBean)
- Ko'p ulanish = kop resurs, oshirib yubormang
