# Bean Lifecycle - Bean qanday tugiladi va oladi?

## Bean nima?

Bean - bu Spring boshqaradigan obekt. Spring yaratgan har qanday obekt "Bean" deb ataladi.

Oddiy qilib aytganda: **Bean = Springning farzandi**. Spring uni yaratadi, parvarish qiladi va kerak bolmasa, yogotadi.

## Beanning hayot davri

Bean 7 bosqichdan otadi. Keling, har birini korib chiqamiz.

```
1. Bean ta'riflanadi (Definition)
          |
2. Bean yaratiladi (Instantiation)
          |
3. Bogliqliklar yuklanadi (Populate Properties)
          |
4. Aware interfeyslari chaqiriladi (Aware)
          |
5. Before init (BeanPostProcessor before)
          |
6. Init metodi (Initialization)
          |
7. After init (BeanPostProcessor after)
          |
         ... Bean ishlatiladi ...
          |
8. Bean yogotiladi (Destruction)
```

## 1-bosqich: Bean ta'riflanadi

Spring qaysi beanlarni yaratish kerakligini aniqlaydi.

```java
@Component  -> @Service  -> @Repository  -> @Controller
     |            |             |               |
     v            v             v               v
     Spring: "Bu 4 ta klassni bean qilib yarataman"
```

Yoki XML orqali:

```xml
<bean id="userService" class="com.example.UserService"/>
```

Yoki @Bean orqali:

```java
@Configuration
public class Config {
    @Bean
    public UserService userService() {
        return new UserService();
    }
}
```

## 2-bosqich: Bean yaratiladi (Instantiation)

Spring Java reflection orqali obektni yaratadi.

```java
// Spring taxminan shunday ishlaydi
Class<?> clazz = Class.forName("com.example.UserService");
Object bean = clazz.getDeclaredConstructor().newInstance();
```

## 3-bosqich: Bogliqliklar yuklanadi

Spring @Autowired yoki konstruktor orqali bogliqliklarni yuklaydi.

```java
public UserService(UserRepository repository) {
    // Spring: "Mana UserRepository, berdim"
    this.repository = repository;
}
```

## 4-bosqich: Aware interfeyslari

Agar bean ma'lum bir Aware interfeysini implement qilgan bolsa, Spring u orqali malumot beradi:

```java
@Component
public class MyBean implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        // Spring: "Mana ApplicationContext ni ham berdim"
        this.applicationContext = context;
    }
}
```

## 5-6-7-bosqich: Init va PostProcessors

@PostConstruct - bean tayyor bolganda ishlaydi:

```java
@Component
public class DatabaseConnection {
    @PostConstruct  // Bean tayyor, bir marta ishga tush
    public void connect() {
        System.out.println("Databasaga ulanildi");
    }
}
```

## 8-bosqich: Bean yogotiladi

ApplicationContext yopilganda yoki bean hayot davri tugaganda:

```java
@Component
public class DatabaseConnection {
    @PreDestroy  // Bean olishidan oldin
    public void disconnect() {
        System.out.println("Database uzildi");
    }
}
```

## Bean lifecycle ni korish

```java
@Component
public class LifecycleDemo {
    
    public LifecycleDemo() {
        System.out.println("1. Constructor: Bean yaratildi");
    }
    
    @Autowired
    public void setDependency(Dependency dep) {
        System.out.println("2. Bogliqlik yuklandi");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("3. @PostConstruct: Boshlangich sozlamalar");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("4. @PreDestroy: Bean ketadi");
    }
}
```

Natija:
```
1. Constructor: Bean yaratildi
2. Bogliqlik yuklandi
3. @PostConstruct: Boshlangich sozlamalar
   ... dastur ishlaydi ...
4. @PreDestroy: Bean ketadi
```

## Bean scope'lari

Bean necha marta yaratilishi scope ga bogliq:

| Scope | Necha marta yaratiladi | Qachon ishlatiladi |
|-------|----------------------|-------------------|
| singleton | 1 marta (default) | Umumiy xizmatlar |
| prototype | Har safar | Holatli obektlar |
| request | Har bir HTTP so'rovda | Web ilovalar |
| session | Har bir sessiyada | Web ilovalar |
| application | Servlet kontekstida | Web ilovalar |

```java
@Component
@Scope("prototype")  // Har safar yangi obekt
public class ShoppingCart {
    // Har bir foydalanuvchi uchun alohida savat
}
```

## Xulosa

Bean lifecycle = Spring beanning tugilishidan olgungacha bolgan jarayon.

- Spring beanni yaratadi
- Bogliqliklarni yuklaydi
- @PostConstruct ishlaydi
- Bean ishlatiladi
- @PreDestroy ishlaydi
- Bean yogotiladi
