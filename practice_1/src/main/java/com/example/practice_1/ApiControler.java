package com.example.practice_1;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class ApiControler {

    @PostMapping("/post")
    public String helloWorld() {
        return "hello_world";
    }
}





