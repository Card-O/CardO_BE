package com.example.testserver.chatgpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatgptService {

    private final WebClient webClient;

    @Autowired
    public ChatgptService(WebClient.Builder webClientBuilder,@Value("${chatgpt.api.key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String makePromotionPrompt(String when, String where, String what, String how) {
        return when + ", " + where + ", " + what + ", " + how + " 내용의 홍보 문자를 만들어 주세요.";
    }

    public String makeTranslationPrompt(String what) {
        return "단어 '" + what + "'를 영어로 번역해 주세요. 결과는 단어만 제공해 주세요.";
    }

    public String makeImagePrompt(String translatedtext) {
        return translatedtext + ", minimalist style ,no text no numbers.";
    }

    public Mono<String> getChatGptResponse(String userMessage) {

        // 요청 본문 설정
        String requestBody = String.format("{\"model\":\"gpt-4\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", userMessage);

        // API 호출
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        return jsonNode.get("choices").get(0).get("message").get("content").asText();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse API response", e);
                    }
                });
    }
}
