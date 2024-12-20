package com.example.testserver.FrontLink;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
@EnableWebFlux
public class FrontConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/auth/**") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://3.104.109.104:4173") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

        registry.addMapping("/chat") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://3.104.109.104:4173") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);


        registry.addMapping("/image/**") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://3.104.109.104:4173") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/ppuriosend") // API 경로에 대한 CORS 설정
                .allowedOrigins("http://3.104.109.104:4173") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");


        registry.addMapping("/address/**")
                .allowedOrigins("http://3.104.109.104:4173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
