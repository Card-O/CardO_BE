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
        String url = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2-1"; // stable-diffusion
        String extprompt = "A clean and organized card design with simple decorative borders, a minimalist background, and a blank space for adding text.";
        String fprompt = prompt + extprompt;
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+ apiKey);
        System.out.println("Using API Key: " + apiKey);
        // 요청 본문 설정 (JSON 형식)
        String requestBody = String.format("{\"prompt\":\"%s\",\"negative_prompt\":\"no text,no numbers\",\"width\":64,\"height\":64}", fprompt);
        System.out.println(requestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // API 호출
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, requestEntity, byte[].class);

        return response.getBody();



    }
}