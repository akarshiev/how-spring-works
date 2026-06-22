# Connection Pool — HikariCP

Ma'lumotlar bazasiga ulanish yaratish — qimmat operatsiya. TCP ulanish, SSL握握手, autentifikatsiya — har birida 1-5ms ketadi. Connection Pool bu muammoni hal qiladi.

## Muammo: har safar yangi ulanish

```
So'rov keldi
    → TCP ulanish oching (1-2ms)
    → TLS握握手 (2-5ms)
    → DB autentifikatsiya (1-2ms)
    → SQL yuborish (0.1ms)
    → Javob olish
    → Ulanishni yoping
```

Millionlab so'rovda bu katastrofa. Pool yechimi:

```
Ilova ishga tushganda: 5-10 ulanish oching va saqlang
    |
So'rov keldi → Pool'dan tayyor ulanish oling (0.01ms)
    |
SQL yuborish → Javob olish → Ulanishni yopmasdan Pool'ga qaytaring
```

## HikariCP

Spring Boot'da default connection pool — HikariCP. Eng tez, eng ishonchli. Alohida qo'shish shart emas — `spring-boot-starter-data-jpa` bilan keladi.

![HikariCP Performance](https://github.com/brettwooldridge/HikariCP/wiki/HikariCP-bench-2.6.png)

## Asosiy sozlamalar

```properties
# Eng muhim sozlamalar
spring.datasource.hikari.pool-name=MyAppPool
spring.datasource.hikari.maximum-pool-size=10          # Maksimal ulanishlar soni
spring.datasource.hikari.minimum-idle=5                 # Minimal bo'sh ulanishlar
spring.datasource.hikari.connection-timeout=30000       # 30 soniya kutish (ms)
spring.datasource.hikari.idle-timeout=600000            # Bo'sh ulanish 10 daqiqadan so'ng yopiladi
spring.datasource.hikari.max-lifetime=1800000           # Ulanish maksimal 30 daqiqa yashaydi
spring.datasource.hikari.keepalive-time=300000          # Har 5 daqiqada "tirikmi?" tekshirish
```

## Qancha ulanish kerak?

Mashhur formula:

```
connections = (CPU core * 2) + effective_spindle_count

SSD disk uchun:
connections = (CPU core * 2) + 1
```

| Server | Tavsiya |
|--------|---------|
| 2 yadro, SSD | 5-10 ulanish |
| 4 yadro, SSD | 10-15 ulanish |
| 8 yadro, SSD | 15-25 ulanish |

Muhim: ko'p ulanish har doim yaxshi emas. DB'ga juda ko'p parallel so'rov — DB'ni sekinlashtiradi. Optimal son topilmaguncha load testing qiling.

## Java konfiguratsiya

```java
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("postgres");
        config.setPassword(System.getenv("DB_PASSWORD"));
        config.setDriverClassName("org.postgresql.Driver");

        // Pool sozlamalari
        config.setPoolName("MyAppPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30_000);    // 30 sekund
        config.setIdleTimeout(600_000);         // 10 daqiqa
        config.setMaxLifetime(1_800_000);       // 30 daqiqa
        config.setKeepaliveTime(300_000);       // 5 daqiqa

        // PostgreSQL optimizatsiya
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Ulanishni test qilish
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }
}
```

## Pool holatini monitoring qilish

```java
@Component
@Slf4j
public class PoolMonitor {

    private final DataSource dataSource;

    @Scheduled(fixedRate = 60_000)  // Har daqiqada
    public void logPoolStats() {
        if (dataSource instanceof HikariDataSource hikari) {
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            log.info("Pool [{}]: total={}, active={}, idle={}, waiting={}",
                hikari.getPoolName(),
                pool.getTotalConnections(),
                pool.getActiveConnections(),
                pool.getIdleConnections(),
                pool.getThreadsAwaitingConnection()
            );
        }
    }
}
```

## Spring Boot Actuator bilan

```properties
management.endpoints.web.exposure.include=health,metrics
```

```
GET /actuator/metrics/hikaricp.connections
GET /actuator/metrics/hikaricp.connections.active
GET /actuator/metrics/hikaricp.connections.pending
```

## Ko'p uchraydigan xatolar

`Connection is not available, request timed out after 30000ms` — pool to'lib ketgan. `maximum-pool-size` ni oshiring yoki so'rovlarni optimallashtiring.

`HikariPool-1 - Failed to validate connection` — DB ulanishi uzilgan. `keepalive-time` va `connection-test-query` sozlang.

`Unable to acquire JDBC Connection` — DB ishlamayapti yoki network muammo. Monitoring va alerting o'rnating.
