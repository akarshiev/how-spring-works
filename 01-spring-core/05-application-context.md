# ApplicationContext ichida nima bor?

ApplicationContext — Spring IoC Containerning asosiy interfeysi. Oddiy qilib aytganda: ilovaning markaziy boshqaruvchisi.

## ApplicationContext vs BeanFactory

Spring da ikkita container bor. `BeanFactory` — oddiy, faqat bean saqlaydi. `ApplicationContext` — to'liq versiya, ko'proq imkoniyat:

| Xususiyat | BeanFactory | ApplicationContext |
|-----------|-------------|-------------------|
| Bean boshqarish | Ha | Ha |
| AOP qo'llab-quvvatlash | Yo'q | Ha |
| Event tizimi | Yo'q | Ha |
| i18n (tarjima) | Yo'q | Ha |
| Environment sozlamalari | Yo'q | Ha |
| Lazy/Eager loading | Lazy | Eager (default) |

Qoida oddiy: har doim `ApplicationContext` ishlating. `BeanFactory` faqat juda cheklangan resurslarda kerak bo'ladi — bu hol amalda deyarli uchramaydi.

## ApplicationContext tarkibi

```
ApplicationContext
      |
      +-- BeanFactory (bean ombori)
      +-- MessageSource (xabarlar va tarjima)
      +-- ResourceLoader (fayllarni yuklash)
      +-- ApplicationEventPublisher (hodisalar tizimi)
      +-- Environment (sozlamalar va profil)
```

## Bean ni olish

```java
ApplicationContext context = SpringApplication.run(Application.class, args);

// Tip bo'yicha olish (tavsiya etiladi)
UserService service = context.getBean(UserService.class);

// Nom bo'yicha olish
UserService service = (UserService) context.getBean("userService");

// Barcha bir turdagi beanlarni olish
Map<String, UserService> allServices = context.getBeansOfType(UserService.class);
```

## Event tizimi

Spring ichida sodir bo'ladigan hodisalarni kuzatish imkoniyati:

```java
// 1. Hodisani yaratish
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;

    public UserRegisteredEvent(Object source, String email) {
        super(source);
        this.email = email;
    }

    public String getEmail() { return email; }
}

// 2. Hodisani tinglash
@Component
public class EmailNotificationListener {

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // User ro'yxatdan o'tganda email yuborish
        emailService.sendWelcome(event.getEmail());
    }
}

// 3. Hodisani chiqarish
@Service
public class UserService {

    private final ApplicationEventPublisher publisher;

    public UserService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public User register(String email) {
        User user = userRepository.save(new User(email));
        // Hodisani e'lon qilish — listener avtomatik ishlaydi
        publisher.publishEvent(new UserRegisteredEvent(this, email));
        return user;
    }
}
```

Bu yondashuv — `UserService` `EmailNotificationListener` ni bilmaydi. Ular bog'liq emas, lekin bir-biri bilan ishlaydi.

## Environment va sozlamalar

```java
@Autowired
private Environment env;

public void printConfig() {
    String dbUrl = env.getProperty("spring.datasource.url");
    String[] profiles = env.getActiveProfiles();
    boolean isProd = env.acceptsProfiles(Profiles.of("prod"));

    System.out.println("DB: " + dbUrl);
    System.out.println("Profil: " + Arrays.toString(profiles));
}
```

## Context turlari

```java
// 1. Spring Boot (avtomatik yaratadi — siz buni ishlatmaysiz)
ApplicationContext ctx = SpringApplication.run(Application.class, args);

// 2. Annotation asosida (test yoki standalone ilovalar uchun)
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

// 3. XML asosida (eski loyihalar)
ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
```

Spring Boot loyihasida `ApplicationContext` ni o'zingiz yaratmaysiz — `SpringApplication.run()` buni qiladi.

## Context ni tekshirish

```java
@Component
public class ContextInspector implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... args) {
        System.out.println("Jami beanlar soni: " + context.getBeanDefinitionCount());
        // Barcha bean nomlarini chiqarish
        Arrays.stream(context.getBeanDefinitionNames())
              .sorted()
              .forEach(name -> System.out.println("  " + name));
    }
}
```

Bu kodni ishga tushirganingizda Spring qancha bean yaratganini ko'rasiz — oddiy ilovada ham 100+ bean bo'lishi mumkin, chunki Spring Boot o'zi ko'p narsani avtomatik yaratadi.
