package com.chatgenius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly configure paths that should be handled as static resources
        registry.addResourceHandler("/static/**")
               .addResourceLocations("classpath:/static/")
               .setCachePeriod(3600);
        
        // No need to explicitly handle WebSocket paths as they should be handled by WebSocketConfig
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Add any SPA fallback routes here if needed
    }
} 