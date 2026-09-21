package com.example.practice_2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    @Bean
    ApplicationRunner logProps(AppProperties props, Environment env) {
        return args -> log.info(
                "Active profiles={} | app.name={} | app.pageSize={} | feature={}",
                String.join(",", env.getActiveProfiles()),
                props.name(), props.pageSize(), props.feature());
    }
}