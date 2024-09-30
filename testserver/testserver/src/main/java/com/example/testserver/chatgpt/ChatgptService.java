package com.example.testserver.chatgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatgptService {

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public ChatgptService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getChatGptResponse(String userMessage) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";

        // 요청 본문 설정
        String requestBody = String.format("{\"model\":\"gpt-4\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                userMessage);

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // API 호출
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 응답 처리
        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new RuntimeException("Failed to call API: " + response.getStatusCode());
        }
    }
}
