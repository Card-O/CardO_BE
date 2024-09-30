package com.example.testserver.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public ResponseEntity<Map<String, Object>> getWeather(@RequestParam String q) {

        String weatherData = weatherService.getWeather(q).block();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(weatherData);
            } catch (JsonProcessingException e) {
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error processing weather data"));

            }

            // 원하는 데이터 추출
            String description = jsonNode.path("weather").get(0).path("description").asText();
            double temperature = jsonNode.path("main").path("temp").asDouble();

            // 모델에 데이터 추가
        Map<String, Object> response = new HashMap<>();
        response.put("city", q);
        response.put("description", description != null ? description : "No description available");
        response.put("temperature", temperature);

        return ResponseEntity.ok(response);}
}