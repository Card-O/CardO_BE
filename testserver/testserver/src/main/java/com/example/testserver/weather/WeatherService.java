package com.example.testserver.weather;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient webClient;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getWeather(String city) {
        String apiKey = "5a118e23903ac0b7bace58895fc54b2a"; // 여기서 YOUR_API_KEY를 실제 API 키로 변경
        return webClient.get()
                .uri("/weather?q={city}&appid={apiKey}", city, apiKey) // OpenWeatherAPI의 날씨 엔드포인트
                .retrieve()
                .bodyToMono(String.class);
    }
}