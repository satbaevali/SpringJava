package com.example.practice_2;

import com.example.practice_2.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Practice2Application {

    public static void main(String[] args) {
        SpringApplication.run(Practice2Application.class, args);
    }

}