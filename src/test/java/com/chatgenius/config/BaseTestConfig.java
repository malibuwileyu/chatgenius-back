package com.chatgenius.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.chatgenius.model")
@EnableJpaRepositories(basePackages = "com.chatgenius.repository")
@EnableTransactionManagement
@Profile("test")
public class BaseTestConfig {
    // Configuration class to enable component scanning and auto-configuration for tests
} 