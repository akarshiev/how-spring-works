# HTTP Basics - HTTP metodlari va status kodlari

## HTTP nima?

HTTP = HyperText Transfer Protocol. Bu ikki kompyuter Internet orqali gaplashadigan til.

Tasavvur qiling: siz restoranga borasiz.

```
Siz (Client) -> "Menga lag'mon bering" -> Ofitsiant (Server)
Ofitsiant (Server) -> "Mana lag'moningiz" -> Siz (Client)
```

HTTP ham shunday ishlaydi:

```
Sizning ilovangiz (Client) -> HTTP so'rov -> Server (Spring)
Server (Spring) -> HTTP javob -> Sizning ilovangiz
```

## HTTP metodlari

HTTP da 5 ta asosiy "harakat" bor. Bular REST API da CRUD (Create, Read, Update, Delete) amallariga togri keladi.

| HTTP metodi | Ma'nosi | CRUD | Nima qiladi? |
|------------|---------|------|-------------|
| GET | Olish | Read | Malumotni oqish |
| POST | Yaratish | Create | Yangi malumot yaratish |
| PUT | Yangilash | Update | Malumotni ozgartirish (to'liq) |
| PATCH | Qisman yangilash | Update | Malumotning bir qismini ozgartirish |
| DELETE | Ochirish | Delete | Malumotni ochirish |

### GET - Malumot olish

```java
@GetMapping("/users")       // GET /users
public List<User> getUsers() {
    return userService.findAll();
}

@GetMapping("/users/{id}")  // GET /users/5
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

### POST - Yangi malumot yaratish

```java
@PostMapping("/users")      // POST /users
public User createUser(@RequestBody User user) {
    return userService.save(user);
}
```

### PUT - Malumotni to'liq yangilash

```java
@PutMapping("/users/{id}")  // PUT /users/5
public User updateUser(@PathVariable Long id, @RequestBody User user) {
    return userService.update(id, user);
}
```

### DELETE - Malumotni ochirish

```java
@DeleteMapping("/users/{id}") // DELETE /users/5
public void deleteUser(@PathVariable Long id) {
    userService.delete(id);
}
```

## HTTP status kodlari

Server sizga javob qaytarganda, javob bilan birga status kodi ham keladi.

### 2xx - Muvaffaqiyat (Success)

| Kod | Ma'nosi | Qachon ishlatiladi? |
|-----|---------|-------------------|
| 200 | OK | GET so'rovi muvaffaqiyatli bolganda |
| 201 | Created | POST bilan yangi resurs yaratilganda |
| 204 | No Content | DELETE muvaffaqiyatli bolganda (javob yoq) |

```java
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok(user);  // 200 OK
}

@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User saved = userService.save(user);
    return ResponseEntity.status(201).body(saved);  // 201 Created
}

@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
}
```

### 4xx - Client xatosi

| Kod | Ma'nosi | Qachon ishlatiladi? |
|-----|---------|-------------------|
| 400 | Bad Request | Malumotlar xato | 
| 401 | Unauthorized | Kirish mumkin emas (login talab) |
| 403 | Forbidden | Ruxsat yoq | 
| 404 | Not Found | Resurs topilmadi |
| 409 | Conflict | Malumotlar ziddiyati |

```java
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ResponseEntity.notFound().build();  // 404
    }
    return ResponseEntity.ok(user);  // 200
}
```

### 5xx - Server xatosi

| Kod | Ma'nosi |
|-----|---------|
| 500 | Internal Server Error (serverda xatolik) |
| 502 | Bad Gateway |
| 503 | Service Unavailable |

## URL va metod birikmasi

| So'rov | Nima qiladi? |
|--------|-------------|
| GET /users | Hamma userlarni olish |
| GET /users/5 | 5-id li userni olish |
| POST /users | Yangi user yaratish |
| PUT /users/5 | 5-id li userni yangilash |
| DELETE /users/5 | 5-id li userni ochirish |
| GET /users/5/orders | 5-id li userni orderlarini olish |

## Xulosa

- GET -> olish
- POST -> yaratish
- PUT -> yangilash
- DELETE -> ochirish
- 2xx -> hammasi yaxshi
- 4xx -> sizda xatolik
- 5xx -> serverda xatolik
