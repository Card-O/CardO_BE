package com.example.testserver.ppurio;

import java.awt.Graphics2D;
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
import java.util.Arrays;

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

	    System.out.println("이미지 크기" + imageBytes.length);

	    System.out.println("Received Image Data (First 100 Bytes):"+ Arrays.toString(Arrays.copyOf(imageBytes, Math.min(imageBytes.length,	100))));

            try {
                // PNG 이미지를 BufferedImage로 변환
                BufferedImage pngImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		
		 if (pngImage == null) {
            // 이미지 읽기 실패 로그
            System.out.println("이미지 읽기 실패. imageBytes 내용: " + Arrays.toString(imageBytes));
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생"));
        }	
		 // 이미지가 RGBA 포맷일 경우 알파 채널 제거 (투명도 제거)
   		// 이미지 타입 확인
System.out.println("Original PNG image type: " + pngImage.getType());

// 이미지가 RGBA 포맷일 경우 알파 채널 제거 (투명도 제거)
if (pngImage.getType() == BufferedImage.TYPE_INT_ARGB || pngImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
    System.out.println("투명도 제거 작업 시작");

    // 원본 이미지 크기
    System.out.println("원본 이미지 크기: " + pngImage.getWidth() + "x" + pngImage.getHeight());

    // RGB 이미지 생성
    BufferedImage rgbImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(), BufferedImage.TYPE_INT_RGB);

    // Graphics2D 객체 생성
    Graphics2D g = rgbImage.createGraphics();

    // 이미지 그리기 전에 확인
    System.out.println("그리기 전: PNG 이미지 크기: " + pngImage.getWidth() + "x" + pngImage.getHeight());
    System.out.println("그리기 전: RGB 이미지 크기: " + rgbImage.getWidth() + "x" + rgbImage.getHeight());

    // 투명도 제거 작업 수행
    g.drawImage(pngImage, 0, 0, null);
    g.dispose();  // Graphics2D 객체 리소스 해제

    // 처리된 이미지를 rgbImage로 교체
    pngImage = rgbImage;

    // 투명도 제거 후 이미지 크기 확인
    System.out.println("투명도 제거 후 이미지 크기: " + pngImage.getWidth() + "x" + pngImage.getHeight());
} else {
    System.out.println("이미지는 이미 RGBA 형식이 아닙니다. 투명도 제거 작업 생략");
}

// 그레이스케일 이미지일 경우 RGB로 변환
    if (pngImage.getType() == 6) {
        System.out.println("그레이스케일 이미지 처리: RGB로 변환");
        BufferedImage rgbImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(pngImage, 0, 0, null);  // 그레이스케일 이미지를 RGB 이미지로 변환
        g.dispose();
        pngImage = rgbImage;  // 변환된 RGB 이미지를 pngImage로 교체
    }



                // JPEG로 변환
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                boolean iiw = ImageIO.write(pngImage, "jpeg", outputStream);

		if (!iiw) {
    System.out.println("이미지 JPEG로 변환 실패");
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생"));
}
                 byte[] jpegBytes = outputStream.toByteArray();
		System.out.println("JPEG로변환된 이미지 크기:"+jpegBytes.length);

		if (jpegBytes.length == 0) {
    System.out.println("이미지 변환 후 크기가 0입니다. 변환에 실패했을 수 있습니다.");
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 변환 실패"));
}

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
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생: " + e.getMessage()));
            }
        }).onErrorResume(e -> {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 처리 중 오류 발생: " + e.getMessage()));
        });
    }
}
