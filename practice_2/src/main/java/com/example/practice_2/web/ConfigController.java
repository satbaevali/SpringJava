package com.example.practice_2.web;

import com.example.practice_2.config.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    private final AppProperties props;

    public ConfigController(AppProperties props) {
        this.props = props;
    }

    @GetMapping("/api/config")
    public AppProperties show() {
        return props;
    }
}