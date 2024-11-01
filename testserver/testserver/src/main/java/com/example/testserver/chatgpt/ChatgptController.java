package com.example.testserver.chatgpt;

import com.example.testserver.aiimage.ImageGenerationService;
import com.example.testserver.aiimage.ImageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatgptController {
    private final ChatgptService chatGptService;
    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;

    public ChatgptController(ChatgptService chatGptService, ImageGenerationService imageGenerationService, ImageService imageService) {
        this.chatGptService = chatGptService;
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
    }

    @GetMapping
    public String getchat() {
        return "chat";
    }

    @PostMapping
    public Mono<String> chat(@RequestBody Map<String, String> data) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    System.out.println("Current SecurityContext: " + securityContext);
                    System.out.println("[" + System.currentTimeMillis() + "] 프론트로부터 POST 요청을 받고 매핑된 메서드가 실행되었습니다.");

                    String time = data.get("time"); // 홍보 날짜
                    String target = data.get("target"); // 홍보 대상
                    String what = data.get("what"); // 홍보 물체의 무엇을 홍보하는가
                    String atmosphere = data.get("atmosphere");

                    System.out.println(atmosphere);
                    String promotionPrompt = chatGptService.makePromotionPrompt(time, target, what);
                    String translationPrompt = chatGptService.makeTranslationPrompt(target);

                    System.out.println("홍보 문자 생성 요청이 시작됩니다.");

                    return chatGptService.getChatGptResponse(promotionPrompt)
                            .flatMap(promotionText -> {
                                System.out.println("performAsyncTask 실행전");

                                // 비동기 작업 수행
                                performAsyncTask(translationPrompt, atmosphere, securityContext) // SecurityContext를 매개변수로 전달
                                        .subscribe(
                                                result -> {
                                                    System.out.println("비동기 작업 완료: " + result);
                                                },
                                                error -> {
                                                    System.out.println("비동기 작업 중 오류 발생: " + error.getMessage());
                                                }
                                        );

                                System.out.println("performAsyncTask 실행후");
                                return Mono.just(promotionText);
                            });
                });
    }

    public Mono<String> performAsyncTask(String translationPrompt, String atmosphere, SecurityContext securityContext) {
        // SecurityContext를 메서드 매개변수로 받아 처리
        Authentication authentication = securityContext.getAuthentication();
        String username = authentication.getName(); // 사용자 이름

        System.out.println("performAsyncTask 시작 직전의 SecurityContext: " + securityContext);
        System.out.println("홍보 대상 키워드 번역 요청이 시작됩니다. 사용자: " + username);
        System.out.println("번역 프롬프트: " + translationPrompt);

        return chatGptService.getChatGptResponse(translationPrompt)
                .flatMap(translatedText -> {
                    System.out.println("getChatGptResponse 호출 완료. 번역된 텍스트: " + translatedText);

                    if (translatedText != null) {
                        String cleanedText = translatedText.replace("'", "");
                        System.out.println(cleanedText); // LOG
                        System.out.println("홍보 대상 키워드 번역 요청이 완료되었습니다.");

                        String imagePrompt = chatGptService.makeImagePrompt(cleanedText, atmosphere);
                        System.out.println("이미지 프롬프트: " + imagePrompt);
                        System.out.println("이미지 프롬프트를 Dall-E로 보내어 이미지 URL 생성을 시작합니다.");

                        return imageGenerationService.generateImage(imagePrompt)
                                .flatMap(imageUrls -> {
                                    for (int i = 0; i < imageUrls.length; i++) {
                                        System.out.println("이미지 URL " + i + "번: " + imageUrls[i]);
                                    }
                                    System.out.println("이미지 URL을 DB에 저장을 시작합니다.");
                                    return imageService.saveImageUrls(imageUrls)
                                            .then(Mono.just("비동기 작업 완료"));
                                });
                    } else {
                        return Mono.just("translatedText is null");
                    }
                })
                .doOnError(e -> {
                    System.out.println("비동기 작업 중 오류 발생: " + e.getMessage());
                });
    }
}