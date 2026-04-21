# IoC Container qanday ishlaydi?

## Inversion of Control - bu nima?

"Inversion of Control" degan qorqinchli soz. Aslida oddiy narsa.

Oddiy dasturda siz obektlarni ozingiz yaratasiz. Bu "Control" (nazorat) sizda. IoC bilan esa bu nazorat Springga otadi. Ya'ni "Control" teskari boladi (Inversion).

## Real hayot misoli

Oddiy hayotda:

- **Siz** non yopmoqchi bolsangiz, **ozingiz** un, suv, tuz aralashtirasiz -> **ozingiz** tandirga ursiz
- Bu sizning "Control"ingizda

IoC bilan:

- Siz faqat "non kerak" deysiz -> **Nonvoy** (Spring) hamma narsani tayyorlab beradi
- "Control" nonvoyda (Springda)

## Kod bilan tushuntirish

### Springsiz (Control sizda):

```java
public class Main {
    public static void main(String[] args) {
        // Siz hamma narsani ozingiz qilasiz
        DatabaseConfig config = new DatabaseConfig("localhost", "5432");
        DataSource dataSource = new DataSource(config);
        UserRepository repository = new UserRepository(dataSource);
        UserService service = new UserService(repository);
        
        service.findUser(1L);
    }
}
```

Bu yerda siz obektlarni qachon va qanday yaratishni boshqaryapsiz.

### Spring bilan (Control Springda):

```java
@Service  // Spring: "Bu UserService ni men boshqaraman"
public class UserService {
    private final UserRepository repository;
    
    // Spring: "Unga UserRepository kerak ekan, beraman"
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

@Configuration faylida Springga aytasiz:

```java
@Configuration
public class AppConfig {
    @Bean
    public UserService userService() {
        return new UserService(userRepository());
    }
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
}
```

Yoki undan ham osoni - Spring Bootda:

```java
@SpringBootApplication  // Bu Springga: "Hamma narsani ozing qil"
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## IoC Container qanday ishlaydi?

IoC Container - bu Springning yuragi. U 3 ta asosiy ish qiladi:

1. **Obektlarni topadi** - Qaysi obektlarni (beanlarni) yaratish kerakligini aniqlaydi
2. **Obektlarni yaratadi** - Topilgan obektlarni yaratadi
3. **Obektlarni ulaydi** - Bir obektga ikkinchi obekt kerak bolsa, ularni ulaydi

```
@SpringBootApplication -> Spring skanerlaydi
        |
        v
@Service, @Repository, @Component -> Topilgan belgilar (annotations)
        |
        v
IoC Container -> Obektlarni yaratadi va saglaydi
        |
        v
@Autowired -> Obektlarni kerakli joyga ulaydi
```

## IoC Container ichida nima bor?

IoC Container - bu oddiy HashMap (Kalit -> Qiymat) dan iborat.

- **Kalit** -> bean nomi (masalan: "userService")
- **Qiymat** -> obektning oz (masalan: UserService obekti)

```java
// Springning IoC Containeri taxminan shunday ishlaydi
public class SimpleIoCContainer {
    private Map<String, Object> beans = new HashMap<>();
    
    public void registerBean(String name, Object bean) {
        beans.put(name, bean);
    }
    
    public Object getBean(String name) {
        return beans.get(name);
    }
}
```

## Xulosa

IoC Container = Springning ombori. Siz unga "menga UserService kerak" deysiz, u sizga tayyor obektni beradi. Qanday yaratilgani, qachon yaratilgani sizni qiziqtirmaydi. Spring hammasini ozi qiladi.
