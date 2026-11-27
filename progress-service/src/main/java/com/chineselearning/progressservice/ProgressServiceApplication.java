package com.chineselearning.progressservice;

import com.chineselearning.progressservice.config.ContentServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ContentServiceProperties.class)
public class ProgressServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgressServiceApplication.class, args);
    }
}