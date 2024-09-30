package com.example.testserver.aiimage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ImageController {

    private final ImageGenerationService imageGenerationService;

    public ImageController(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    @PostMapping("/generate-image")
    public byte[] generateImage(@RequestBody String prompt) {
        // 내부적으로 /api/images/generate 호출
        byte[] imagedata = imageGenerationService.generateImage(prompt);

        if (imagedata == null || imagedata.length == 0) {
            System.out.println("Image data is empty or null.");
        } else {
            System.out.println("Image data retrieved successfully, size: " + imagedata.length);
        }

        return imagedata;

    }

}