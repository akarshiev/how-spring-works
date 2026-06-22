# Integration Testing — @SpringBootTest, TestContainers

Unit test bitta klassni izolyatsiyada tekshiradi. Integration test esa bir nechta qismni — Controller, Service, Repository, ma'lumotlar bazasini — birgalikda tekshiradi.

## Unit Test vs Integration Test

Unit test tez, izolyatsiyada, mock bilan. Integration test sekin, haqiqiy komponentlar bilan, ma'lumotlar bazasi bilan.

Ikkalasi ham kerak: unit test logikani, integration test qatlamlar o'rtasidagi aloqani tekshiradi.

## @SpringBootTest — to'liq ilova

```java
@SpringBootTest  // Butun Spring kontekstini ishga tushiradi
@Transactional   // Har bir test rollback bo'ladi — DB tozalanadi
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserAndFindById() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Ali", "ali@test.com", "pass123");

        // When
        UserResponse registered = userService.register(request);

        // Then — haqiqiy DB'dan tekshirish
        Optional<User> found = userRepository.findById(registered.getId());
        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Ali", "duplicate@test.com", "pass");
        userService.register(request);

        // When & Then
        assertThrows(DuplicateEmailException.class,
            () -> userService.register(request));
    }
}
```

## @DataJpaTest — faqat JPA qatlami

Controller va Service yuklanmaydi — faqat Repository va DB:

```java
@DataJpaTest  // Faqat JPA beanlarini yuklaydi, H2 ishlatadi (yoki ko'rsatilgan DB)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = new User("Ali", "ali@test.com");
        User user2 = new User("Vali", "vali@test.com");
        userRepository.saveAll(List.of(user1, user2));
    }

    @Test
    void shouldFindByEmail() {
        Optional<User> found = userRepository.findByEmail("ali@test.com");

        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }

    @Test
    void shouldReturnEmptyForNonExistentEmail() {
        Optional<User> found = userRepository.findByEmail("notexist@test.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmailShouldReturnTrueForExistingEmail() {
        assertTrue(userRepository.existsByEmail("ali@test.com"));
        assertFalse(userRepository.existsByEmail("unknown@test.com"));
    }
}
```

## @WebMvcTest — faqat Controller qatlami

Service mock qilinadi, faqat HTTP qatlami tekshiriladi:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getUserShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(1L, "Ali", "ali@test.com");
        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Ali"))
            .andExpect(jsonPath("$.email").value("ali@test.com"))
            .andDo(print());  // Konsolga chiqarish (debug uchun)
    }

    @Test
    void createUserShouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ali", "ali@test.com", "pass123");
        UserResponse response = new UserResponse(1L, "Ali", "ali@test.com");
        when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserShouldReturn404WhenNotFound() throws Exception {
        when(userService.findById(999L))
            .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createUserWithInvalidEmailShouldReturn400() throws Exception {
        String invalidRequest = """
            {"name": "Ali", "email": "notanemail", "password": "pass123"}
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").exists());
    }
}
```

## Testcontainers — haqiqiy PostgreSQL

`@DataJpaTest` default H2 (in-memory) ishlatadi. Production'da PostgreSQL — farqlar bo'lishi mumkin. Testcontainers Docker'da haqiqiy PostgreSQL ishga tushiradi:

```xml
<!-- pom.xml -->
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
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

```java
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Replace.NONE = H2 o'rniga haqiqiy DB ishlatiladi
class UserRepositoryContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        User user = new User("Ali", "ali@test.com");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }
}
```

Container bir marta ishga tushadi (`static`) — barcha testlar uchun qayta ishlatiladi. Tez.

## Test konfiguratsiya — application-test.properties

```properties
# src/test/resources/application-test.properties
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.hibernate.SQL=DEBUG
```

```java
@SpringBootTest
@ActiveProfiles("test")  // application-test.properties faollashtiradi
class ServiceIntegrationTest { ... }
```

## @Sql — test ma'lumotlarini yuklash

```sql
-- src/test/resources/test-data.sql
INSERT INTO users (id, name, email, password, role, is_active, created_at, updated_at)
VALUES (1, 'Ali', 'ali@test.com', 'encoded', 'USER', true, NOW(), NOW()),
       (2, 'Admin', 'admin@test.com', 'encoded', 'ADMIN', true, NOW(), NOW());
```

```java
@SpringBootTest
@Sql("/test-data.sql")  // Testdan oldin SQL yuklash
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserServiceTest {

    @Test
    void shouldFindAllUsers() {
        List<UserResponse> users = userService.findAll(0, 10).getContent();
        assertEquals(2, users.size());
    }
}
```

## Test piramidasi

Ko'p unit test, o'rtacha integration test, kam end-to-end test — bu optimal nisbat. Integration test sekin va resurs talab qiladi, lekin real integratsiyani tekshiradi.

Unit test: tez, ko'p, logikani tekshiradi. Integration test: sekin, kam, komponentlar aloqasini tekshiradi. E2E test: eng sekin, eng kam, to'liq scenariyni tekshiradi.
