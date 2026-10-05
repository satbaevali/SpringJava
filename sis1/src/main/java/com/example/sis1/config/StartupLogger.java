package com.example.sis1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
@Component
public class StartupLogger implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);
    private final AppProperties appProperties;
    private final Environment environment;

    public StartupLogger(AppProperties appProperties,Environment environment){
        this.appProperties = appProperties;
        this.environment = environment;

    }
    @Override
    public void run(String... args){
        boolean smsEnabled = appProperties.feature() !=null && appProperties.feature().smsEnabled();
        log.info("==================================================================");
        log.info(">>> Активные профили: {} | app.name='{}' | app.pageSize={} | smsEnabled={} <<<",
                Arrays.toString(environment.getActiveProfiles()),
                appProperties.name(),
                appProperties.pageSize(),
                smsEnabled);
        log.info("==================================================================");
    }

}
