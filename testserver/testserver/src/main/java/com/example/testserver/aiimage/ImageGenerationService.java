package com.example.testserver.aiimage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ImageGenerationService {

    private final RestTemplate restTemplate;

    @Value("${huggingface.api.key}")
    private String apiKey; // 여기에 실제 API 키를 입력하세요.

    @Autowired
    public ImageGenerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] generateImage(String prompt) {
        String url = "https://api-inference.huggingface.co/models/CompVis/stable-diffusion-v1-4"; // DeepAI API URL

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+ apiKey);
        System.out.println("Using API Key: " + apiKey);
        // 요청 본문 설정 (JSON 형식)
        String requestBody = String.format("{\"inputs\":\"%s\"}", prompt);
        System.out.println(requestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // API 호출
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, requestEntity, byte[].class);

        return response.getBody();



    }
}