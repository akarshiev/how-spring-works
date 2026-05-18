# Unit Testing - JUnit 5 asoslari

## Unit Test nima?

Unit Test = kichik bir qismni (metodni) alohida tekshirish.

Tasavvur qiling, siz mashina yasayapsiz. Har bir detalni alohida tekshirasiz:

- Dvigatelni tekshirasiz (ozicha)
- Tormozni tekshirasiz (ozicha)
- Rulni tekshirasiz (ozicha)

Va shundan keyin hammasini yig'asiz. Unit test aynan shu.

## JUnit 5 asoslari

```java
// 1-test: oddiy test
class CalculatorTest {
    
    @Test
    void shouldAddTwoNumbers() {
        Calculator calculator = new Calculator();
        
        int result = calculator.add(2, 3);
        
        assertEquals(5, result);  // "2+3=5 bolishi kerak"
    }
}
```

## Test tuzilishi (Given-When-Then)

Har bir test 3 qismdan iborat:

```java
@Test
void shouldCalculateTotalPrice() {
    // 1. Given - tayyorgarlik
    Order order = new Order();
    order.addItem(new Item("Laptop", 2000));
    order.addItem(new Item("Mouse", 50));
    
    // 2. When - harakat
    BigDecimal total = order.calculateTotal();
    
    // 3. Then - tekshirish
    assertEquals(new BigDecimal("2050"), total);
}
```

## Eng kop ishlatiladigan assert lar

```java
@Test
void allAssertions() {
    String name = "Ali";
    int age = 25;
    boolean isActive = true;
    String[] fruits = {"apple", "banana"};
    
    assertEquals("Ali", name);          // Tenglik
    assertNotEquals("Vali", name);      // Teng emas
    assertTrue(isActive);               // true
    assertFalse(!isActive);             // false
    assertNull(null);                   // null
    assertNotNull(name);                // null emas
    assertArrayEquals(fruits, new String[]{"apple", "banana"});  // Massiv teng
    assertThrows(Exception.class, () -> { throw new Exception(); }); // Exception
}
```

## @BeforeEach va @AfterEach

```java
class UserServiceTest {
    
    private UserService userService;
    
    @BeforeEach  // Har bir testdan oldin ishlaydi
    void setUp() {
        userService = new UserService();
        System.out.println("Testga tayyor");
    }
    
    @AfterEach  // Har bir testdan keyin ishlaydi
    void tearDown() {
        System.out.println("Test tugadi");
    }
    
    @Test
    void test1() { }
    
    @Test
    void test2() { }
}
```

## @BeforeAll va @AfterAll

```java
class DatabaseTest {
    
    @BeforeAll  // Hamma testlardan oldin 1 marta ishlaydi (static)
    static void initDatabase() {
        // Database ni ishga tushirish
    }
    
    @AfterAll  // Hamma testlardan keyin 1 marta ishlaydi (static)
    static void closeDatabase() {
        // Database ni yopish
    }
}
```

## Service test misol

```java
class UserServiceTest {
    
    private UserService userService;
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);  // Mock
        userService = new UserService(userRepository);
    }
    
    @Test
    void shouldFindUserById() {
        // Given
        User user = new User(1L, "Ali", "ali@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When
        User result = userService.findById(1L);
        
        // Then
        assertEquals("Ali", result.getName());
        verify(userRepository).findById(1L);  // repository chaqirilganmi?
    }
}
```

## Xulosa

- Unit Test = kichik qismni alohida tekshirish
- @Test -> test metodi
- assertEquals, assertTrue -> tekshirishlar
- @BeforeEach, @AfterEach -> har bir testdan oldin/keyin
- Given-When-Then -> test tuzilishi
