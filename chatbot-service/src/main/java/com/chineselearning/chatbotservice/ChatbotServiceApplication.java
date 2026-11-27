package com.chineselearning.chatbotservice;

import com.chineselearning.chatbotservice.config.ChatbotProperties;
import com.chineselearning.chatbotservice.config.GeminiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GeminiProperties.class, ChatbotProperties.class})
public class ChatbotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotServiceApplication.class, args);
    }
}