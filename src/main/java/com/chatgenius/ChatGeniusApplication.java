package com.chatgenius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.chatgenius.model")
@EnableJpaRepositories(basePackages = "com.chatgenius.repository")
public class ChatGeniusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatGeniusApplication.class, args);
    }

} 