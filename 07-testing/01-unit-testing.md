# Unit Testing — JUnit 5 asoslari

Unit test — kichik bir qismni (metod yoki klass) izolyatsiyada tekshirish. Bug'larni erta topish va kodni o'zgartirganda hech narsa buzilmaganligini tasdiqlash uchun zarur.

## Nima uchun test yozish kerak?

Kod yozganda hamma narsa to'g'ri ishlaydi. 3 oy o'tib, boshqa xususiyat qo'shganda — nima buzilganini topish qiyin. Test har safar barcha qismlar hali ham to'g'ri ishlashini tekshiradi.

## JUnit 5 — asosiy sintaksis

```java
class CalculatorTest {

    @Test
    void twoAndThreeShouldEqualFive() {
        Calculator calculator = new Calculator();

        int result = calculator.add(2, 3);

        assertEquals(5, result);
    }

    @Test
    void dividingByZeroShouldThrowException() {
        Calculator calculator = new Calculator();

        assertThrows(ArithmeticException.class, () -> calculator.divide(10, 0));
    }
}
```

## Given-When-Then pattern

Har bir test uchta qismdan iborat:

```java
@Test
void activeUserCountShouldReturnOnlyActiveUsers() {
    // Given — tayyorgarlik
    User activeUser1 = new User("Ali", "ali@example.com", true);
    User activeUser2 = new User("Vali", "vali@example.com", true);
    User inactiveUser = new User("Guli", "guli@example.com", false);
    userRepository.saveAll(List.of(activeUser1, activeUser2, inactiveUser));

    // When — sinab ko'rilayotgan harakat
    long count = userService.countActiveUsers();

    // Then — tekshirish
    assertEquals(2, count);
}
```

## Assertion'lar

```java
@Test
void assertionsExample() {
    User user = new User(1L, "Ali", "ali@example.com");
    List<String> roles = List.of("USER", "ADMIN");

    assertEquals("Ali", user.getName());           // Tenglik
    assertNotEquals("Vali", user.getName());       // Teng emas
    assertTrue(user.isActive());                   // true
    assertFalse(user.getName().isEmpty());         // false
    assertNull(user.getPhone());                   // null
    assertNotNull(user.getEmail());                // null emas
    assertInstanceOf(String.class, user.getName()); // Tip
    assertEquals(2, roles.size());                 // To'plam o'lchami

    // Exception
    assertThrows(UserNotFoundException.class,
        () -> userService.findById(999L));

    // Bir nechta assertion — birinchi muvaffaqiyatsiz bo'lsa ham davom etadi
    assertAll("user fields",
        () -> assertEquals(1L, user.getId()),
        () -> assertEquals("Ali", user.getName()),
        () -> assertEquals("ali@example.com", user.getEmail())
    );
}
```

## Lifecycle metodlari

```java
class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeAll   // Barcha testlar boshlashidan oldin — 1 marta (static)
    static void initDatabase() {
        System.out.println("Test ma'lumotlar bazasi tayyor");
    }

    @BeforeEach  // Har bir testdan oldin
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @AfterEach   // Har bir testdan keyin
    void tearDown() {
        // Tozalash
    }

    @AfterAll    // Barcha testlar tugagandan keyin — 1 marta (static)
    static void cleanUp() {
        System.out.println("Ma'lumotlar bazasi yopildi");
    }
}
```

## Test nomlash

Test nomi muvaffaqiyatsiz bo'lganda muammoni tushuntirishi kerak:

```java
// Yaxshi nom — nima tekshirilayotgani aniq
@Test
void registerShouldThrowExceptionWhenEmailAlreadyExists() { }

@Test
void findByIdShouldReturnUserWhenUserExists() { }

@Test
void calculateTotalShouldIncludeDiscountForPremiumUsers() { }

// Yomon nom — hech narsa aytmaydi
@Test
void test1() { }

@Test
void registerTest() { }
```

## Parametrlangan test

Bir xil testni ko'p qiymat bilan:

```java
@ParameterizedTest
@ValueSource(strings = {"ali@email.com", "user@domain.org", "name@company.net"})
void validEmailsShouldPassValidation(String email) {
    assertTrue(EmailValidator.isValid(email));
}

@ParameterizedTest
@ValueSource(strings = {"notanemail", "@domain.com", "user@", ""})
void invalidEmailsShouldFailValidation(String email) {
    assertFalse(EmailValidator.isValid(email));
}

@ParameterizedTest
@CsvSource({
    "2, 3, 5",
    "0, 0, 0",
    "-1, 1, 0",
    "100, 200, 300"
})
void addShouldReturnCorrectSum(int a, int b, int expected) {
    assertEquals(expected, calculator.add(a, b));
}
```

## @DisplayName — o'qilishi oson nom

```java
@DisplayName("UserService testlari")
class UserServiceTest {

    @Test
    @DisplayName("Foydalanuvchi mavjud bo'lganda ID bo'yicha topishi kerak")
    void shouldFindUserByIdWhenExists() { }

    @Test
    @DisplayName("Foydalanuvchi topilmaganda UserNotFoundException tashlashi kerak")
    void shouldThrowExceptionWhenUserNotFound() { }
}
```

## @Disabled — vaqtincha o'chirish

```java
@Test
@Disabled("Hali to'g'ri SQL yozilmagan — #123 issue")
void shouldCalculateComplexReport() { }
```

## Assumption — shart bo'lmasa test o'tkazib yuborish

```java
@Test
void shouldRunOnlyInLinux() {
    assumeTrue(System.getProperty("os.name").contains("Linux"),
        "Bu test faqat Linux'da ishlaydi");

    // Linux'da bajariladi, boshqasida o'tkazib yuboriladi
}
```
