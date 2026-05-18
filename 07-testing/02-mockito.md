# Mockito - Mock, Stub, @MockBean

## Mock nima?

Mock = soxta obekt. Haqiqiy obektni "taqlid qiladigan" obekt.

Nega mock kerak?

- Malumotlar bazasiga bogliq metodni test qilish
- Email yuborishni test qilish (haqiqiy email yubormasdan)
- Tashqi API ga bogliq kodni test qilish

## @Mock - Soxta obekt yaratish

```java
// Mock yaratish
@Mock
private UserRepository userRepository;

// Yoki
private UserRepository userRepository = mock(UserRepository.class);
```

## @InjectMocks - Mock larni ulash

```java
// UserService ga @Mock larni avtomatik yuklaydi
@InjectMocks
private UserService userService;
```

To'liq misol:

```java
@ExtendWith(MockitoExtension.class)  // Mockito ni yoqish
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks  // UserService ga mock larni yuklaydi
    private UserService userService;
    
    @Test
    void shouldRegisterUser() {
        // Given
        User user = new User("Ali", "ali@example.com");
        when(userRepository.save(any(User.class)))     // Stub
            .thenReturn(new User(1L, "Ali", "ali@example.com"));
        
        // When
        User result = userService.register(user);
        
        // Then
        assertEquals(1L, result.getId());
        assertEquals("Ali", result.getName());
        
        // Tekshirish: save metodi 1 marta chaqirilganmi?
        verify(userRepository, times(1)).save(any(User.class));
        
        // Tekshirish: emailService.sendWelcome 1 marta chaqirilganmi?
        verify(emailService, times(1)).sendWelcome("ali@example.com");
    }
}
```

## Stub - Mock ga javob berish

Stub = "agar bu metod shu parametr bilan chaqirilsa, mana shu javobni qaytar"

```java
// -------- WHEN (qachon) --------

// 1. Qiymat qaytarish
when(repository.findById(1L)).thenReturn(Optional.of(user));
when(repository.findAll()).thenReturn(List.of(user1, user2));

// 2. Exception tashlash
when(repository.findById(999L)).thenThrow(new UserNotFoundException(999L));

// 3. Bir nechta javob
when(repository.findById(1L))
    .thenReturn(Optional.of(user1))     // 1-marta
    .thenReturn(Optional.of(user2));    // 2-marta

// 4. Har qanday argument bilan
when(repository.save(any(User.class))).thenReturn(user);
when(repository.findById(anyLong())).thenReturn(Optional.of(user));

// 5. Aniq argument bilan
when(repository.save(eq(user))).thenReturn(user);
```

## Verify - Mock ni tekshirish

Verify = "bu metod chaqirilganmi, necha marta chaqirilgan?"

```java
// -------- VERIFY (tekshirish) --------

// 1. 1 marta chaqirilgan
verify(repository).save(user);
verify(repository, times(1)).save(user);

// 2. 2 marta chaqirilgan
verify(repository, times(2)).save(user);

// 3. Hech qachon chaqirilmagan
verify(repository, never()).delete(any());

// 4. Kamida 1 marta
verify(repository, atLeast(1)).findById(1L);

// 5. Eng kop 3 marta
verify(repository, atMost(3)).findAll();
```

## @MockBean - Spring Boot da mock

Spring Boot testlarida:

```java
@SpringBootTest
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean  // Spring contextdagi haqiqiy bean ni mock ga almashtiradi
    private UserService userService;
    
    @Test
    void shouldReturnUser() throws Exception {
        // Given
        when(userService.findById(1L))
            .thenReturn(new UserResponse(1L, "Ali", "ali@example.com"));
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ali"));
    }
}
```

## @Spy - Qisman mock

```java
@ExtendWith(MockitoExtension.class)
class SpyExampleTest {
    
    @Spy  // Haqiqiy obekt, lekin ayrim metodlarni mock qilish mumkin
    private List<String> list = new ArrayList<>();
    
    @Test
    void shouldUseSpy() {
        // Haqiqiy metod ishlaydi
        list.add("Ali");
        assertEquals(1, list.size());
        
        // Mock qilish
        when(list.size()).thenReturn(100);
        assertEquals(100, list.size());  // Haqiqiy emas, mock javob
    }
}
```

## Xulosa

- @Mock -> soxta obekt yaratadi
- @InjectMocks -> mock larni obyektga yuklaydi
- when().thenReturn() -> stub (javob berish)
- verify() -> chaqirilganligini tekshirish
- @MockBean -> Spring testlarda haqiqiy bean ni almashtiradi
- @Spy -> haqiqiy obekt, ayrim metodlari mock
