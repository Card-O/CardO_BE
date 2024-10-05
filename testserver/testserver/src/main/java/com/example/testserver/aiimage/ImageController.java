package com.example.testserver.aiimage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class ImageController {

    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;

    public ImageController(ImageGenerationService imageGenerationService, ImageService imageService) {
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
    }

    @PostMapping("/generate-imageurl")
    public String[] generateImageUrl(@RequestBody String prompt) {
        // 내부적으로 /api/images/generate 호출
        String[] imageURL = imageGenerationService.generateImage(prompt);

        return imageURL;

    }

    public void ImageURLSave(String[] imageURLs) {
        imageService.saveImageUrls(imageURLs);
    }

    @GetMapping("/generate-image")
    public String generateImage() {
        return imageService.getSmallestIDurl();
    }


}