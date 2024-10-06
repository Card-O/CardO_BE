package com.example.testserver.aiimage;

import com.example.testserver.DB.ImageRepository;
import com.example.testserver.chatgpt.ChatgptController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/image")
public class ImageController {
    private long id = 1;

    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public ImageController(ImageGenerationService imageGenerationService, ImageService imageService, ImageRepository imageRepository) {
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    @PostMapping("/generate-imageurl")
    public String[] generateImageUrl(@RequestBody String prompt) {

        String[] imageURL = imageGenerationService.generateImage(prompt);

        return imageURL;

    }

    public void ImageURLSave(String[] imageURLs) {
        imageService.saveImageUrls(imageURLs);
    }

    @GetMapping("/generate-image")
    public String generateImage() {
        String url = imageService.findUrlById(id);
        return url;
    }

    @GetMapping("/generate-next-image")
    public String generatenextImage() {
        id++;
        String url = imageService.findUrlById(id);
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