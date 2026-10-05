package com.example.sis1;

import com.example.sis1.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Sis1Application {

    public static void main(String[] args) {
        SpringApplication.run(Sis1Application.class, args);
    }

}