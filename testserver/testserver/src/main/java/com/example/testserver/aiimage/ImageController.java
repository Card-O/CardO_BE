package com.example.testserver.aiimage;

import com.example.testserver.DB.ImagePromptRepository;
import com.example.testserver.DB.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;


@RestController
@RequestMapping("/image")
public class ImageController {
    private final ImagePromptRepository imagePromptRepository;
    private final UserRepository userRepository;
    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;


    public ImageController(ImageGenerationService imageGenerationService, ImageService imageService, UserRepository userRepository,ImagePromptRepository imagePromptRepository) {
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.imagePromptRepository = imagePromptRepository;
    }

    @PostMapping("/generate-imageurl")
    public Mono<String[]> generateImageUrl(@RequestBody String prompt) {
        Mono<String[]> imageURL = imageGenerationService.generateImage(prompt);
        return imageURL;
    }

    @GetMapping("/generate-image")
    public Mono<ResponseEntity<String>> generateImage() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Mono<String> urlMono = imageService.findLowestImgUrlByUserId(securityContext);
                    Mono<Integer> imgnumMono = imageService.findLowestImgNumByUserId(securityContext);

                    return userRepository.findByUsername(securityContext.getAuthentication().getName())
                            .flatMap(user -> {
                                Long userid = user.getId();
                                // Mono.zip을 사용하여 urlMono와 imgnumMono를 결합
                                return Mono.zip(urlMono, imgnumMono)
                                        .map(tuple -> {
                                            String url = tuple.getT1(); // 첫 번째 Mono의 값
                                            Integer imgnum = tuple.getT2(); // 두 번째 Mono의 값
                                            String jsonResponse = String.format("{\"url\": \"%s\", \"imgnum\": %d, \"userid\": %d}", url, imgnum, userid);
                                            System.out.println("Sending url:"+url);
                                            System.out.println("JSON Response: " + jsonResponse);
                                            return ResponseEntity.ok(jsonResponse); // HTTP 200 OK 응답
                                        });
                            });
                });
    }

    @PostMapping("/generate-next-image")
    public Mono<ResponseEntity<String>> generatenextImage(@RequestParam Long userid,@RequestParam Integer imgnum) {
        Integer lin = imgnum + 1;
        System.out.println("userid:"+userid+"imgnum:"+lin);
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Mono<String> nextUrl = imageService.findNextUrlById(lin)
                            .switchIfEmpty(Mono.just("다음 URL이 없습니다."));

                    if (lin % 3 == 2) {  // 2번째 이미지를 보고 있는 경우
                        System.out.println("count is 2, 5, 8....");

                        // 비동기적으로 이미지 프롬프트를 가져온 후 다음 작업을 수행
                        imagePromptRepository.findImagePromptByUserId(userid)
                                .flatMap(imageprompt -> {
                                    System.out.println("Get Imgprompt caused by % 3 ==2: " + imageprompt);

                                    // 이미지 생성 요청
                                    return imageGenerationService.generateImage(imageprompt)
                                            .flatMap(imageUrls -> {
                                                for (int i = 0; i < imageUrls.length; i++) {
                                                    System.out.println("이미지 URL " + i + "번: " + imageUrls[i]);
                                                }
                                                System.out.println("이미지 URL을 DB에 저장을 시작합니다.");
                                                // DB에 이미지 URL 저장
                                                return imageService.saveImageUrls(imageUrls, securityContext)
                                                        .doOnSuccess(user -> System.out.println("사용자 정보: " + user));
                                            });
                                })
                                .subscribe(); // 모든 비동기 처리가 완료된 후 subscribe
                    }

                    return nextUrl.map(url -> {
                        // JSON 형식으로 응답 생성
                        String jsonResponse = String.format("{\"url\": \"%s\", \"in\": %d}", url, lin);
                        System.out.println("Sending url:"+url);
                        return ResponseEntity.ok(jsonResponse);  // HTTP 200 OK 응답
                    });
                });

        }
}