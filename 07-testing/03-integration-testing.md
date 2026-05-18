# Integration Testing - @SpringBootTest, TestContainers

## Integration Test nima?

Integration Test = bir nechta qismni birgalikda tekshirish.

Unit Test dan farqi:

| Unit Test | Integration Test |
|-----------|-----------------|
| Bir klassni test qiladi | Bir nechta klassni test qiladi |
| Mock ishlatiladi | Haqiqiy obekt ishlatiladi |
| Tez ishlaydi | Sekin ishlaydi |
| DB kerak emas | Haqiqiy DB kerak |

## @SpringBootTest

@SpringBootTest butun Spring ilovasini ishga tushiradi.

```java
@SpringBootTest  // Spring ilovasini to'liq ishga tushiradi
class UserServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();  // Har testdan oldin tozalash
    }
    
    @Test
    void shouldRegisterAndFindUser() {
        // Given
        User user = new User("Ali", "ali@example.com");
        
        // When
        User saved = userService.register(user);
        
        // Then
        User found = userService.findById(saved.getId());
        assertEquals("Ali", found.getName());
    }
}
```

## @DataJpaTest - Faqat JPA qatlami

```java
@DataJpaTest  // Faqat JPA bilan bogliq beanlarni yuklaydi
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindByEmail() {
        // Given
        User user = new User("Ali", "ali@example.com");
        userRepository.save(user);
        
        // When
        Optional<User> found = userRepository.findByEmail("ali@example.com");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }
    
    @Test
    void shouldCheckIfEmailExists() {
        // Given
        userRepository.save(new User("Ali", "ali@example.com"));
        
        // When
        boolean exists = userRepository.existsByEmail("ali@example.com");
        
        // Then
        assertTrue(exists);
    }
}
```

## @WebMvcTest - Faqat Controller qatlami

```java
@WebMvcTest(UserController.class)  // Faqat UserController ni test qiladi
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void shouldReturnUser() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "Ali", "ali@example.com");
        when(userService.findById(1L)).thenReturn(userResponse);
        
        // When & Then
        mockMvc.perform(get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Ali"))
            .andExpect(jsonPath("$.email").value("ali@example.com"));
    }
    
    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenThrow(new UserNotFoundException(999L));
        
        // When & Then
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound());
    }
}
```

## @TestContainers - Haqiqiy Database

TestContainers = Docker orqali haqiqiy malumotlar bazasini ishga tushirish.

### 1-qadam: Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### 2-qadam: Test configuration

```java
@Testcontainers  // TestContainer larni yoqish
@SpringBootTest
class UserRepositoryTestContainersTest {
    
    @Container  // PostgreSQL container yaratish
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource  // Spring sozlamalarini container ga qaratish
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldSaveAndFindUser() {
        User user = new User("Ali", "ali@example.com");
        userRepository.save(user);
        
        Optional<User> found = userRepository.findByEmail("ali@example.com");
        
        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }
}
```

## @Sql - Test uchun malumot tayyorlash

```sql
-- src/test/resources/users.sql
INSERT INTO users (id, name, email) VALUES (1, 'Ali', 'ali@example.com');
INSERT INTO users (id, name, email) VALUES (2, 'Vali', 'vali@example.com');
```

```java
@SpringBootTest
@Sql("/users.sql")  // Testdan oldin SQL ni ishga tushirish
class UserServiceTest {
    
    @Test
    void shouldFindAllUsers() {
        List<User> users = userService.findAll();
        assertEquals(2, users.size());
    }
}
```

## Xulosa

- @SpringBootTest -> butun ilovani test qilish
- @DataJpaTest -> faqat JPA qatlami
- @WebMvcTest -> faqat Controller
- @MockBean -> haqiqiy bean ni mock ga almashtirish
- TestContainers -> haqiqiy DB da test qilish
- @Sql -> test uchun malumot tayyorlash
