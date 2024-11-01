package com.example.testserver.aiimage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageGenerationService {

    private final WebClient webClient;

    @Value("${chatgpt.api.key}")
    private String apiKey; // 여기에 실제 API 키를 입력하세요.

    @Autowired
    public ImageGenerationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build(); // 기본 URL 설정
    }

    public Mono<String[]> generateImage(String prompt) {
        // 요청 본문 설정 (JSON 형식)
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        body.put("n", 3); // 이미지 개수
        body.put("size", "512x512"); // 이미지 크기
//        body.put("model", "dall-e-3");
//        body.put("prompt", prompt);
//        body.put("n", 1); // 이미지 개수
//        body.put("size", "1024x1024"); // 이미지 크기

        System.out.println("Request Body: " + body);


        // API 호출
        return webClient.post()
                .uri("/images/generations")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, String>> data = (List<Map<String, String>>) response.get("data");
                    String[] imageUrls = new String[data.size()]; // 크기가 3인 String 배열

                    for (int i = 0; i < data.size(); i++) {
                        imageUrls[i] = data.get(i).get("url");
                    }
                    return imageUrls; // 이미지 URL 배열 반환
                });
    }
}