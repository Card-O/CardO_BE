package com.example.testserver.ppurio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RequestService {
    private static final Integer TIME_OUT = 5000;
    private static final String API_KEY = "3ced3188fd29949dcb0a9c90f86658a87d6042ca97c5aa32eda5e1c6ed70bc22";
    private static final String PPURIO_ACCOUNT = "gong";
    private static final String URI = "https://message.ppurio.com";

    public void requestSend(MessageRequestDTO messageRequestDTO) {
        String basicAuthorization = Base64.getEncoder().encodeToString((PPURIO_ACCOUNT + ":" + API_KEY).getBytes());

        Map<String, Object> tokenResponse = getToken(URI, basicAuthorization); // 토큰 발급
        Map<String ,Object> sendResponse = send(URI, (String) tokenResponse.get("token"),messageRequestDTO); // 발송 요청

        System.out.println(sendResponse.toString());
    }

    /**
     * Access Token 발급 요청 (한 번 발급된 토큰은 24시간 유효합니다.)
     * @param baseUri 요청 URI ex) https://message.ppurio.com
     * @param BasicAuthorization "계정:연동 개발 인증키"를 Base64 인코딩한 문자열
     * @return Map
     */

    private Map<String, Object> getToken(String baseUri, String BasicAuthorization) {
        HttpURLConnection conn = null;
        try {
            // 요청 파라미터 생성
            Request request = new Request(baseUri + "/v1/token", "Basic " + BasicAuthorization);

            // 요청 객체 생성
            conn = createConnection(request);

            // 응답 데이터 객체 변환
            return getResponseBody(conn);
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 문자 발송 요청
     * @param baseUri 요청 URI ex) https://message.ppurio.com
     * @param accessToken 토큰 발급 API를 통해 발급 받은 Access Token, 유효기간이 1일이기 때문에 만료될 경우 재발급 필요
     * @return Map
     */
    private Map<String, Object> send(String baseUri, String accessToken,MessageRequestDTO messageRequestDTO) {
        HttpURLConnection conn = null;
        try {
            // 요청 파라미터 생성
            String bearerAuthorization = String.format("%s %s", "Bearer", accessToken);
            Request request = new Request(baseUri + "/v1/message", bearerAuthorization);

            // 요청 객체 생성
            conn = createConnection(request, createSendTestParams(messageRequestDTO));// sms 발송 테스트
            Map<String, Object> response = getResponseBody(conn);
            System.out.println("API Response: " + response);
            // 응답 데이터 객체 변환
            return response;
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private <T> HttpURLConnection createConnection(Request request, T requestObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInputString = objectMapper.writeValueAsString(requestObject);
        // 요청 객체 생성
        HttpURLConnection connect = createConnection(request);
        connect.setDoOutput(true); // URL 연결을 출력용으로 사용(true)
        // 요청 데이터 처리
        try (OutputStream os = connect.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connect;
    }

    private HttpURLConnection createConnection(Request request) throws IOException {
        URL url = new URL(request.getRequestUri());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", request.getAuthorization()); // Authorization 헤더 입력
        conn.setConnectTimeout(TIME_OUT); // 연결 타임아웃 설정(5초)
        conn.setReadTimeout(TIME_OUT); // 읽기 타임아웃 설정(5초)
        return conn;
    }

    private Map<String, Object> getResponseBody(HttpURLConnection conn) {
        InputStream inputStream;

        try {
            if (conn.getResponseCode() == 200) { // 요청 성공
                inputStream = conn.getInputStream();
            } else { // 서버에서 요청은 수신했으나 특정 이유로 인해 실패함
                inputStream = conn.getErrorStream();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                responseBody.append(inputLine);
            }

            // 성공 응답 데이터 변환
            return convertJsonToMap(responseBody.toString());
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    private Map<String, Object> convertJsonToMap(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<>() {});
    }

    private Map<String, Object> createSendTestParams(MessageRequestDTO messageRequestDTO) throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("account", PPURIO_ACCOUNT);
        params.put("messageType", "MMS");
        params.put("from", messageRequestDTO.getSendNumber());
        params.put("content", messageRequestDTO.getPromotiontext().toString());
        params.put("duplicateFlag", "Y");
        params.put("rejectType", "AD"); // 광고성 문자 수신거부 설정, 비활성화할 경우 해당 파라미터 제외
        params.put("targetCount", messageRequestDTO.getReceiveNumbers().size());

        List<Map<String, String>> targets = new ArrayList<>();
        for (String number : messageRequestDTO.getReceiveNumbers()) {
            // 각 전화번호를 "to" 키로 가지는 Map 생성
            Map<String, String> target = Map.of("to", number);
            targets.add(target); // 리스트에 추가
        }
        params.put("targets", targets);

        HashMap<String,Object> filesparams = encodeImageToBase64(messageRequestDTO.getImage());

        params.put("files", List.of(filesparams));
        params.put("refKey", RandomStringUtils.random(32, true, true)); // refKey 생성, 32자 이내로 아무 값이든 상관 없음
        return params;
    }

    private Map<String, Object> createFileTestParams(String filePath) throws RuntimeException, IOException {
        FileInputStream fileInputStream = null;
        try {
            File file = new File(filePath);
            byte[] fileBytes = new byte[ (int) file.length()];
            fileInputStream = new FileInputStream(file);
            int readBytes = fileInputStream.read(fileBytes);

            if (readBytes != file.length()) {
                throw new IOException();
            }

            String encodedFileData = Base64.getEncoder().encodeToString(fileBytes);

            HashMap<String, Object> params = new HashMap<>();
            params.put("size", file.length());
            params.put("name", file.getName());
            params.put("data", encodedFileData);
            return params;
        } catch (IOException e) {
            throw new RuntimeException("파일을 가져오는데 실패했습니다.", e);
        } finally {
            if(fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    public HashMap<String, Object> encodeImageToBase64(byte[] jpegBytes) {

	if (jpegBytes == null || jpegBytes.length == 0) {
        throw new RuntimeException("이미지 데이터가 비어 있습니다.");
    }
        // Base64 인코딩
        String encodedFileData = Base64.getEncoder().encodeToString(jpegBytes);
	 
        // 파일 정보 맵 생성
        HashMap<String, Object> params = new HashMap<>();
        params.put("size", jpegBytes.length);
        params.put("name", "image.jpeg"); // 여기서 이미지 파일 이름을 적절히 설정
        params.put("data", encodedFileData);

        return params;
    }

}

class Request {
    private String requestUri;
    private String authorization;

    public Request(String requestUri, String authorization) {
        this.requestUri = requestUri;
        this.authorization = authorization;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getAuthorization() {
        return authorization;
    }
}
