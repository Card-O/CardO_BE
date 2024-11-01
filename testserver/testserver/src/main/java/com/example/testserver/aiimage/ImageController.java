package com.example.testserver.aiimage;

import com.example.testserver.DB.ImageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;


    public ImageController(ImageGenerationService imageGenerationService, ImageService imageService) {
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
    }

    @PostMapping("/generate-imageurl")
    public Mono<String[]> generateImageUrl(@RequestBody String prompt) {

        Mono<String[]> imageURL = imageGenerationService.generateImage(prompt);

        return imageURL;

    }

    public void ImageURLSave(String[] imageURLs) {
    }

    @GetMapping("/generate-image")
    public Mono<String> generateImage() {
        Mono<String> url = imageService.findLowestImgNumByUserId();
        return url;
    }

    @GetMapping("/generate-next-image")
    public Mono<String> generatenextImage() {
        Mono<String> url = imageService.findNextUrlById();
        return url;
    }

    @DeleteMapping("/deleteallimage")
    public ResponseEntity<String> deleteAllImages() {
        try {
            imageService.deleteallimages(); // 서비스 레이어에서 삭제 처리
            return ResponseEntity.ok("All images deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting images.");
        }
    }


}