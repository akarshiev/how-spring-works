# HTTP Basics — HTTP metodlari va status kodlari

REST API yozishdan oldin HTTP'ni tushunish kerak. HTTP — client va server o'rtasidagi "til".

## HTTP so'rov tuzilishi

```
GET /api/users/5 HTTP/1.1
Host: example.com
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{"name": "Ali"}
```

Birinchi qator — metod + URL + versiya. Keyingi qatorlar — headerlar. Bo'sh qatordan keyin — body (POST, PUT da bo'ladi).

## HTTP metodlari

| Metod | Ma'nosi | CRUD | Xususiyat |
|-------|---------|------|-----------|
| GET | Olish | Read | Body yo'q, xavfsiz, takrorlanadi |
| POST | Yaratish | Create | Body bor, har safar yangi resurs |
| PUT | To'liq yangilash | Update | To'liq resurs yuboriladi |
| PATCH | Qisman yangilash | Update | Faqat o'zgaruvchi qismlar |
| DELETE | O'chirish | Delete | Body yo'q |

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping               // GET /api/users
    public List<User> getAll() { ... }

    @GetMapping("/{id}")      // GET /api/users/5
    public User getById(@PathVariable Long id) { ... }

    @PostMapping              // POST /api/users
    public User create(@RequestBody User user) { ... }

    @PutMapping("/{id}")      // PUT /api/users/5
    public User update(@PathVariable Long id, @RequestBody User user) { ... }

    @PatchMapping("/{id}")    // PATCH /api/users/5
    public User partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> fields) { ... }

    @DeleteMapping("/{id}")   // DELETE /api/users/5
    public void delete(@PathVariable Long id) { ... }
}
```

## HTTP status kodlari

![HTTP Status Codes Overview](https://miro.medium.com/v2/resize:fit:1400/1*w_iicbG7L3xEQTArjHUS6g.jpeg)

### 2xx — Muvaffaqiyat

```java
// 200 OK — GET muvaffaqiyatli
return ResponseEntity.ok(user);

// 201 Created — POST muvaffaqiyatli, yangi resurs yaratildi
return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
// Yaxshiroq — location header bilan
URI location = URI.create("/api/users/" + savedUser.getId());
return ResponseEntity.created(location).body(savedUser);

// 204 No Content — DELETE muvaffaqiyatli, body yo'q
return ResponseEntity.noContent().build();
```

### 4xx — Client xatosi

| Kod | Nomi | Qachon? |
|-----|------|---------|
| 400 | Bad Request | Noto'g'ri ma'lumot yuborildi |
| 401 | Unauthorized | Login qilinmagan |
| 403 | Forbidden | Login qilingan, lekin ruxsat yo'q |
| 404 | Not Found | Resurs topilmadi |
| 409 | Conflict | Email allaqachon mavjud |
| 422 | Unprocessable Entity | Validation xatosi |

```java
// 401 vs 403 farqi — tez-tez chalkashtiriladi
// 401: "Kim ekansiz?" — autentifikatsiya kerak
// 403: "Bilaman kim ekansiz, lekin ruxsat yo'q" — avtorizatsiya xatosi

@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, ex.getMessage()));
}
```

### 5xx — Server xatosi

| Kod | Nomi | Qachon? |
|-----|------|---------|
| 500 | Internal Server Error | Server ichida kutilmagan xato |
| 502 | Bad Gateway | Upstream server javob bermadi |
| 503 | Service Unavailable | Server vaqtincha mavjud emas |

5xx xatolari — server muammosi. Client hech narsa qila olmaydi.

## URL dizayni

Yaxshi REST URL qoidalari:

```
GET    /api/users           <- to'plam (ko'plik)
GET    /api/users/5         <- bitta element
POST   /api/users           <- yangi yaratish
PUT    /api/users/5         <- to'liq yangilash
DELETE /api/users/5         <- o'chirish
GET    /api/users/5/orders  <- bog'liq resurslar (ichki)
```

Yomon URL:
```
GET /api/getUser/5          <- fe'l ishlatmang
GET /api/user/5             <- ko'plikni ishlating
POST /api/users/create      <- metod URL'da emas, HTTP metodda
GET /api/users?action=delete <- DELETE metodini ishlating
```

## Query parametrlar

Filtrlash, sahifalash va saralash uchun:

```
GET /api/users?page=0&size=20&sort=name,asc
GET /api/users?search=ali&role=ADMIN&active=true
GET /api/products?category=electronics&minPrice=100&maxPrice=500
```
