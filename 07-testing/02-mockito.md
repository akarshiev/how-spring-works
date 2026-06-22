# Mockito — Mock, Stub, @MockBean

Unit testda har bir klass izolyatsiyada tekshiriladi. `UserService` testida haqiqiy ma'lumotlar bazasiga ulanmaslik kerak. Mockito soxta obyektlar (mock) yaratish orqali shu muammoni hal qiladi.

## Mock nima?

Mock — real klass o'rniga ishlaydigan "qo'g'irchoq". Qo'ng'iroqlarni yozib oladi va siz belgilagan javoblarni qaytaradi.

```java
// Haqiqiy UserRepository — DB'ga murojaat qiladi
// Mock UserRepository — siz aytganini qaytaradi

UserRepository realRepo = ...;         // DB'ga ulanish, test uchun noqulay
UserRepository mockRepo = mock(UserRepository.class);  // Soxta, biz nazorat qilamiz
```

## @ExtendWith(MockitoExtension.class)

```java
@ExtendWith(MockitoExtension.class)  // Mockito'ni JUnit 5 bilan ulash
class UserServiceTest {

    @Mock
    private UserRepository userRepository;  // Soxta repository

    @Mock
    private EmailService emailService;      // Soxta email service

    @InjectMocks
    private UserService userService;        // @Mock'larni UserService'ga inject qiladi

    @Test
    void shouldReturnUserWhenFound() {
        // Given — stub: "findById(1) chaqirilsa, bu userni qaytar"
        User user = new User(1L, "Ali", "ali@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.findById(1L);

        // Then
        assertEquals("Ali", result.getName());
        assertEquals("ali@example.com", result.getEmail());
    }
}
```

## when().thenReturn() — Stub

```java
// Qiymat qaytarish
when(repository.findById(1L)).thenReturn(Optional.of(user));
when(repository.findAll()).thenReturn(List.of(user1, user2));
when(repository.count()).thenReturn(100L);

// Exception tashlash
when(repository.findById(999L)).thenThrow(new UserNotFoundException(999L));

// Argumentga qarab
when(repository.findById(anyLong())).thenReturn(Optional.of(user));    // Har qanday Long
when(repository.findByEmail(eq("ali@example.com"))).thenReturn(Optional.of(user)); // Aniq

// Ketma-ket turli javoblar
when(repository.findAll())
    .thenReturn(List.of(user1))   // 1-chaqiruvda
    .thenReturn(List.of(user2));  // 2-chaqiruvda

// void metodlar
doNothing().when(emailService).send(anyString(), anyString());
doThrow(new RuntimeException()).when(emailService).send(eq("bad@email.com"), anyString());
```

## verify() — chaqirilganini tekshirish

```java
@Test
void shouldSendEmailAfterRegistration() {
    // Given
    CreateUserRequest request = new CreateUserRequest("Ali", "ali@example.com", "pass123");
    when(userRepository.save(any())).thenReturn(savedUser);

    // When
    userService.register(request);

    // Then — verify
    verify(userRepository, times(1)).save(any(User.class));   // 1 marta chaqirilganmi?
    verify(emailService, times(1)).sendWelcome("ali@example.com");
    verify(userRepository, never()).delete(any());            // Hech qachon chaqirilmaganmi?
}

// Boshqa verify opsiyalari
verify(repo, times(2)).save(any());      // Aniq 2 marta
verify(repo, atLeast(1)).findAll();      // Kamida 1 marta
verify(repo, atMost(3)).count();         // Ko'pi bilan 3 marta
verifyNoInteractions(emailService);      // Umuman chaqirilmaganmi?
verifyNoMoreInteractions(userRepository); // Faqat verify'dagi chaqiruvlar bo'lgan?
```

## ArgumentCaptor — argument qiymatini ushlash

```java
@Test
void savedUserShouldHaveEncodedPassword() {
    // Given
    CreateUserRequest request = new CreateUserRequest("Ali", "ali@example.com", "plainPassword");
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // When
    userService.register(request);

    // Then — save'ga nima berilganini tekshirish
    verify(userRepository).save(userCaptor.capture());

    User capturedUser = userCaptor.getValue();
    assertNotEquals("plainPassword", capturedUser.getPassword());  // Ochiq parol emas
    assertTrue(passwordEncoder.matches("plainPassword", capturedUser.getPassword()));  // Encoded
}
```

## @Spy — qisman mock

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    private UserMapper userMapper = new UserMapperImpl();  // Haqiqiy obyekt

    @Test
    void shouldCallRealMapper() {
        User user = new User(1L, "Ali", "ali@example.com");

        // Haqiqiy metod ishlaydi
        UserResponse response = userMapper.toResponse(user);
        assertEquals("Ali", response.getName());

        // Ayrim metodlarni mock qilish mumkin
        doReturn(new UserResponse()).when(userMapper).toResponse(any());
    }
}
```

## @MockBean — Spring Boot testlarida

Spring'ning haqiqiy bean'ini mock bilan almashtirish:

```java
@WebMvcTest(UserController.class)  // Faqat Web qatlami
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // Spring contextdagi UserService'ni mock bilan almashtiradi
    private UserService userService;

    @Test
    void getUserShouldReturn200WithUserData() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "Ali", "ali@example.com");
        when(userService.findById(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Ali"))
            .andExpect(jsonPath("$.email").value("ali@example.com"));
    }

    @Test
    void getUserShouldReturn404WhenNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenThrow(new UserNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createUserShouldReturn201() throws Exception {
        // Given
        String requestBody = """
            {"name": "Ali", "email": "ali@example.com", "password": "pass123"}
            """;
        UserResponse response = new UserResponse(1L, "Ali", "ali@example.com");
        when(userService.create(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```
