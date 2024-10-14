package com.example.testserver.FrontLink;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FrontConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/auth/**") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

        registry.addMapping("/chat/**") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

        registry.addMapping("/image/**") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

        registry.addMapping("/users") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

    }
}