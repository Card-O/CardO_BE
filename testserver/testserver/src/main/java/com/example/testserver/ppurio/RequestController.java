package com.example.testserver.ppurio;


import com.example.testserver.DB.ImagePromptRepository;
import com.example.testserver.DB.ImageRepository;
import com.example.testserver.aiimage.ImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
public class RequestController {
    private final ImagePromptRepository imagePromptRepository;
    private final ImageService imageService;
    private final RequestService requestService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ImageRepository imageRepository;

    public RequestController(RequestService requestService , ImageService imageService, ImagePromptRepository imagePromptRepository, ImageRepository imageRepository) {
        this.imagePromptRepository = imagePromptRepository;
        this.requestService = requestService;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    @PostMapping(value = "/ppuriosend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> ppuriosend(
            @RequestPart("promotiontext") String promotiontext,
            @RequestPart("sendNumber") String sendNumber,
            @RequestPart("receiveNumbers") String receiveNumbersJson,
            @RequestPart("image") Mono<DataBuffer> imageDataBuffer,
            @RequestPart("userid") String userid)
    {

        List<String> receiveNumbers;
        try {
            // JSON 문자열을 List<String>으로 변환
            receiveNumbers = objectMapper.readValue(receiveNumbersJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format for receiveNumbers"));
        }

        return imageDataBuffer.flatMap(dataBuffer -> {
            // 이미지 데이터 처리
            byte[] imageBytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(imageBytes);

            try {
                // PNG 이미지를 BufferedImage로 변환
                BufferedImage pngImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // JPEG로 변환
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(pngImage, "jpeg", outputStream);
                byte[] jpegBytes = outputStream.toByteArray();

                // 로그 출력
                System.out.println("발송할 데이터:");
                System.out.println("Promotion Text: " + promotiontext);
                System.out.println("Send Number: " + sendNumber);
                System.out.println("Receive Numbers: " + receiveNumbers);
                System.out.println("userid"+ userid);
                // MessageRequestDTO 객체 생성 후 요청
                MessageRequestDTO messageRequestDTO = new MessageRequestDTO(promotiontext, jpegBytes, sendNumber, receiveNumbers);
                requestService.requestSend(messageRequestDTO);

                imageService.deleteallimages(Long.parseLong(userid));
                imagePromptRepository.deleteByUserId(Long.parseLong(userid)).subscribe();

                // 성공적인 응답
                return Mono.just(ResponseEntity.ok("발송 성공"));
            } catch (IOException e) {
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생"));
            }
        }).onErrorResume(e -> {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생"));
        });
    }
}
