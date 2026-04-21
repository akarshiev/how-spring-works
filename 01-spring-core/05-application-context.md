# ApplicationContext ichida nima bor?

## ApplicationContext nima?

ApplicationContext - Springning markaziy interfeysi. Bu IoC Containerning asosiy qismi.

Oddiy qilib: **ApplicationContext = Springning miyasi**. Hamma narsa shu yerda boshqariladi.

## ApplicationContext vs BeanFactory

Springda ikkita asosiy container bor:

| Xususiyat | BeanFactory | ApplicationContext |
|-----------|-------------|-------------------|
| Lazy loading | Ha (kerak paytda yaratadi) | Yoq (hammasini oldindan yaratadi) |
| AOP qollab-quvvatlash | Yoq | Ha |
| Event system | Yoq | Ha |
| i18n (tarjima) | Yoq | Ha |
| Web uchun | Yoq | Ha |

**Qoida:** ApplicationContext ishlating. BeanFactory faqat juda cheklangan resurslarda kerak.

## ApplicationContext ichidagi asosiy qismlar

```
ApplicationContext
      |
      +-- BeanFactory (obecktlar ombori)
      +-- MessageSource (tarjima va xabarlar)
      +-- ResourceLoader (fayllarni yuklash)
      +-- ApplicationEventPublisher (hodisalar)
      +-- Environment (sozlamalar)
```

### 1. BeanFactory - Obektlar ombori

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// Beanni nomi bilan olish
UserService service = (UserService) context.getBean("userService");

// Beanni klassi bilan olish (tavsiya etiladi)
UserService service = context.getBean(UserService.class);

// Bir nechta beanni olish
Map<String, UserService> beans = context.getBeansOfType(UserService.class);
```

### 2. MessageSource - Tarjima va xabarlar

```java
// messages.properties faylidan:
// welcome.message = Xush kelibsiz, {0}!

@Autowired
private MessageSource messageSource;

public void greet(String name) {
    String message = messageSource.getMessage(
        "welcome.message",    // kalit
        new Object[]{name},   // parametrlar
        Locale.ENGLISH        // til
    );
    System.out.println(message); // "Xush kelibsiz, Akbar!"
}
```

### 3. Event System - Hodisalar

Spring ichida sodir boladigan hodisalarni kuzatish:

```java
// Hodisani yaratish
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;
    
    public UserRegisteredEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
    
    public String getEmail() { return email; }
}

// Hodisani eshitish
@Component
public class EmailNotificationListener {
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("Email yuborildi: " + event.getEmail());
    }
}

// Hodisani chiqarish
@Service
public class UserService {
    @Autowired
    private ApplicationEventPublisher publisher;
    
    public void register(String email) {
        // Foydalanuvchini registratsiya qilish
        publisher.publishEvent(new UserRegisteredEvent(this, email));
    }
}
```

### 4. Environment - Sozlamalar

```java
@Autowired
private Environment env;

public void printConfig() {
    String dbUrl = env.getProperty("database.url");
    String[] profiles = env.getActiveProfiles();
    
    System.out.println("DB URL: " + dbUrl);
    System.out.println("Active profile: " + Arrays.toString(profiles));
}
```

## ApplicationContext turlari

Springda 3 xil ApplicationContext bor:

```java
// 1. Annotation bilan (eng keng tarqalgan)
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

// 2. XML bilan (eski usul)
ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

// 3. Web ilovalar uchun
ApplicationContext ctx = new AnnotationConfigWebApplicationContext();
```

## Spring Bootdagi ApplicationContext

Spring Bootda siz ApplicationContext ni ozingiz yaratmaysiz:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // Spring ApplicationContext ni oz-ozidan yaratadi
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        
        // Siz faqat ishlatasiz
        UserService service = ctx.getBean(UserService.class);
    }
}
```

## ApplicationContext ni korish

```java
@Component
public class ContextInspector implements CommandLineRunner {
    
    @Autowired
    private ApplicationContext context;
    
    @Override
    public void run(String... args) {
        System.out.println("=== Beanlar soni: " + context.getBeanDefinitionCount());
        System.out.println("=== Beanlar royxati:");
        
        String[] beanNames = context.getBeanDefinitionNames();
        for (String name : beanNames) {
            System.out.println("  -> " + name);
        }
    }
}
```

## Xulosa

- **ApplicationContext** = Springning markaziy boshqaruvchisi
- Uning ichida **BeanFactory**, **MessageSource**, **Environment** va boshqa qismlar bor
- Siz faqat `@Autowired` qilasiz, qolganini Spring qiladi
- ApplicationContext siz uchun obektlarni topadi, yaratadi va boshqaradi
