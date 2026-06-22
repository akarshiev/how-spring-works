# IoC Container qanday ishlaydi?

IoC (Inversion of Control) — eng muhim tushunchalardan biri. Nomidan qo'rqmang: aslida oddiy narsa.

## Nazorat kimda?

Oddiy dasturda siz obyektlarni o'zingiz yaratasiz. Nazorat **sizda**.

IoC bilan bu nazorat Springga o'tadi. "Control" teskari bo'ladi — shuning uchun "Inversion of Control".

**Springsiz — nazorat sizda:**

```java
public class Main {
    public static void main(String[] args) {
        // Siz qachon, qanday yaratishni boshqarasiz
        DatabaseConfig config = new DatabaseConfig("localhost", "5432");
        DataSource dataSource = new DataSource(config);
        UserRepository repository = new UserRepository(dataSource);
        UserService service = new UserService(repository);

        service.findUser(1L);
    }
}
```

**Spring bilan — nazorat Springda:**

```java
@Service
public class UserService {
    private final UserRepository repository;

    // Spring: "UserService ga UserRepository kerak — beraman"
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Siz faqat `@Service` yozdingiz. Qolganini Spring qildi.

## IoC Container ichida nima bor?

IoC Container — Springning "ombori". Soddalashtirilgan holda — bu katta `HashMap`:

```java
// Spring IoC Container taxminan shunday ishlaydi
public class SimpleIoCContainer {
    private final Map<String, Object> beans = new HashMap<>();

    public void register(String name, Object bean) {
        beans.put(name, bean);
    }

    public Object get(String name) {
        return beans.get(name);
    }
}
```

Kalit — bean nomi (`"userService"`), qiymat — yaratilgan obyekt.

## Uch bosqich

Spring ishga tushganda IoC Container uchta bosqichni bajaradi:

**Birinchi bosqich: Skanerlash.** Spring `@Component`, `@Service`, `@Repository`, `@Controller` bilan belgilangan barcha klasslarni topadi.

**Ikkinchi bosqich: Yaratish.** Topilgan klasslardan obyekt yasaydi.

**Uchinchi bosqich: Ulash.** Har bir obyektga unga kerakli boshqa obyektlarni beradi (Dependency Injection).

```
@SpringBootApplication ishga tushadi
         |
         v
Komponentlar skanerlanadi
         |
         v
IoC Container obyektlarni yaratadi
         |
         v
@Autowired orqali ulaydi
```

## Kod misolida ko'rish

```java
@Configuration
public class AppConfig {

    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }

    @Bean
    public UserService userService() {
        // Spring userRepository() ni IoC Containerdan topib beradi
        return new UserService(userRepository());
    }
}
```

Spring Boot da annotatsiyalar bilan qisqaroq:

```java
@Repository
public class UserRepository { ... }

@Service
public class UserService {
    public UserService(UserRepository repository) { ... }
}
```

Spring Boot o'zi skanerlaydi, o'zi yaratadi, o'zi ulaydi.

## ApplicationContext — IoC Containerning asosiy interfeysi

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);

        // Containerdan to'g'ridan-to'g'ri olish (nadir hollarda kerak)
        UserService service = context.getBean(UserService.class);
    }
}
```

Odatda siz `context.getBean()` chaqirmaysiz — `@Autowired` yoki konstruktor injection orqali Spring o'zi beradi.
