package com.example.testserver.aiimage;

import com.example.testserver.DB.ImageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/image")
public class ImageController {
    private long id = 1;

    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;


    public ImageController(ImageGenerationService imageGenerationService, ImageService imageService) {
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
    }

    @PostMapping("/generate-imageurl") // 프롬프트 받고 이미지 URL 생성
    public Mono<String[]> generateImageUrl(@RequestBody String prompt) {

        Mono<String[]> imageURL = imageGenerationService.generateImage(prompt);

        return imageURL;

    }

    public void ImageURLSave(String[] imageURLs) {
        imageService.saveImageUrls(imageURLs);
    } // 이미지 URL DB에 저장

    @GetMapping("/generate-image") // DB에서 이미지 URL 조회
    public Mono<String> generateImage() {
        Mono<String> url = imageService.findUrlById(id);
        return url;
    }

    @GetMapping("/generate-next-image") // DB에서 다음 이미지 URL 조회
    public Mono<String> generatenextImage() {
        id++;
        Mono<String> url = imageService.findUrlById(id);
        return url;
    }

    @DeleteMapping("/deleteallimage") // DB에 저장된 이미지 삭제
    public ResponseEntity<String> deleteAllImages() {
        try {
            imageService.deleteallimages(); // 서비스 레이어에서 삭제 처리
            return ResponseEntity.ok("All images deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting images.");
        }
    }


}