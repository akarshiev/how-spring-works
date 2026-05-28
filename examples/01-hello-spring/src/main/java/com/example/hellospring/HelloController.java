package com.example.hellospring;

// @RestController = bu klass HTTP so'rovlarni qabul qiladi
// @GetMapping = GET so'rovi kelganda shu metod ishlaydi
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController  // Springga: "Bu klass REST API ga javob beradi"
public class HelloController {

    // GET /hello -> "Hello, Spring!" qaytaradi
    // Misol: curl http://localhost:8080/hello
    @GetMapping("/hello")
    public String sayHello() {
        // Spring bu string ni avtomatik HTTP javobga aylantiradi
        return "Hello, Spring!";
    }

    // GET /hello?name=Ali -> "Hello, Ali!" qaytaradi
    // Misol: curl http://localhost:8080/hello?name=Ali
    @GetMapping("/hello/personal")
    public String sayHelloPersonal(
            // @RequestParam = URL dagi ?name=... ni olish
            // defaultValue = agar name berilmasa, "World" ishlatiladi
            @RequestParam(defaultValue = "World") String name
    ) {
        return "Hello, " + name + "!";
    }
}
