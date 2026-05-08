# Security Basics - Authentication vs Authorization

## Ikki asosiy tushuncha

Spring Security da ikkita eng muhim tushuncha bor:

### Authentication - Kim bu?

"Sen kimsan?" degan savolga javob.

- Foydalanuvchi login va parol yuboradi
- Spring tekshiradi: "Bu login va parol tog'rimi?"
- Agar togri bolsa -> "Siz Alisiz" (authentication boldi)

### Authorization - Nima qila olasan?

"Sen nima qila olasan?" degan savolga javob.

- Foydalanuvchi "/admin" sahifasiga kirmoqchi
- Spring tekshiradi: "Bu foydalanuvchi adminmi?"
- Agar admin bolmasa -> "Sizga ruxsat yoq" (authorization boldi)

## Real hayotda

Tasavvur qiling, siz klubga kirmoqchisiz:

1. **Authentication:** "Passportingizni korsating" -> Pasport tekshiriladi -> "Siz Alisiz, kirishingiz mumkin"
2. **Authorization:** "VIP zonga kirmoqchi edim" -> "Kechirasiz, siz VIP emassiz, kira olmaysiz"

## Spring Security qanday ishlaydi?

Spring Security filterlar orqali ishlaydi. Har bir so'rov bir nechta filterdan otadi.

```
HTTP So'rov
    |
    v
SecurityFilterChain (filterlar zanjiri)
    |
    +-- 1. SecurityContextHolderFilter (kontekstni sozlaydi)
    +-- 2. UsernamePasswordAuthenticationFilter (login formani tekshiradi)
    +-- 3. BasicAuthenticationFilter (Basic Auth ni tekshiradi)
    +-- 4. ExceptionTranslationFilter (xatolarni tarjima qiladi)
    +-- 5. AuthorizationFilter (ruxsatni tekshiradi)
    |
    v
    Controller (agar hamma filterdan o'tgan bolsa)
```

## Security Context - Foydalanuvchi malumotlari

Foydalanuvchi autentifikatsiyadan o'tgandan keyin, uning malumotlari SecurityContext da saqlanadi.

```java
// Foydalanuvchi malumotlarini olish
@GetMapping("/me")
public String currentUser() {
    // 1-usul: SecurityContextHolder dan
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    
    // 2-usul: @AuthenticationPrincipal bilan
    return username;
}

@GetMapping("/me2")
public String currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return userDetails.getUsername();
}
```

## Principal - Foydalanuvchini ifodalash

```java
// Principal = hozirgi foydalanuvchi
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

authentication.getName()      // username
authentication.getAuthorities() // roller (ROLE_USER, ROLE_ADMIN)
authentication.isAuthenticated() // login qilganmi?
```

## Oddiy qilib aytganda:

- **Authentication** -> login va parol tekshirish
- **Authorization** -> ruxsatni tekshirish
- **SecurityContext** -> hozirgi foydalanuvchi malumotlari saqlanadigan joy
- **Filter** -> har bir so'rovni tekshiruvchi
