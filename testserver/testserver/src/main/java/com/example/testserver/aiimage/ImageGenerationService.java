package com.example.testserver.aiimage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageGenerationService {

    private final RestTemplate restTemplate;

    @Value("${chatgpt.api.key}")
    private String apiKey; // 여기에 실제 API 키를 입력하세요.

    @Autowired
    public ImageGenerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] generateImage(String prompt) {
        // String url = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2-1"; // stable-diffusion
        String url ="https://api.openai.com/v1/images/generations"; // DALL-E ENDPOINT
        // String extprompt = "A clean and organized card design with simple decorative borders, a minimalist background.";
        // String fprompt = prompt + extprompt;
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+ apiKey);

        // 요청 본문 설정 (JSON 형식)
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        body.put("n", 1); // 이미지 개수
        body.put("size", "256x256"); // 이미지 크기

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map responseBody = response.getBody();  // 응답 본문이 Map 형태로 반환됨
            if (responseBody != null && responseBody.containsKey("data")) {
                // "data" 필드는 리스트(List) 형태이며, 첫 번째 요소는 이미지 정보를 담은 Map
                String imageurl = ((Map<String, String>) ((List<?>) responseBody.get("data")).get(0)).get("url");
                try {
                    return downloadImageAsBytes(imageurl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }


        return null;
    }

    public byte[] downloadImageAsBytes(String imageUrl) throws Exception {
        // 이미지 URL 객체 생성
        URL url = new URL(imageUrl);

        // HttpURLConnection 객체 생성 및 연결
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // 응답 코드가 200일 때 이미지 다운로드
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // InputStream을 사용해 이미지 데이터를 읽음
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                // 버퍼를 사용해 데이터를 읽고 저장
                byte[] buffer = new byte[1024]; // 버퍼 크기 설정
                int bytesRead;

                // InputStream으로부터 데이터를 읽어서 ByteArrayOutputStream에 쓰기
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                // 바이트 배열로 변환 후 반환
                return byteArrayOutputStream.toByteArray();
            }
        } else {
            throw new RuntimeException("Failed to download image, HTTP response code: " + connection.getResponseCode());
        }


}}