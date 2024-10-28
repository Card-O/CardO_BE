package com.example.testserver.chatgpt;

import com.example.testserver.aiimage.ImageGenerationService;
import com.example.testserver.aiimage.ImageService;
import org.springframework.scheduling.annotation.Async;
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
    private String translatedtext;
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
        System.out.println("[" + System.currentTimeMillis() + "] 프론트로부터 POST 요청을 받고 매핑된 메서드가 실행되었습니다.");

        String time = data.get("time"); // 홍보 날짜
        String target = data.get("target"); // 홍보 대상
        String what = data.get("what"); // 홍보 물체의 무엇을 홍보하는가
        String atmosphere = data.get("atmosphere");

        System.out.println(atmosphere);
        // 첫 번째 요청: 홍보 문자 생성
        String promotionPrompt = chatGptService.makePromotionPrompt(time, target, what);
        // 두 번째 요청: 단어 번역
        String translationPrompt = chatGptService.makeTranslationPrompt(target);

        try { // 홍보 문자 생성
            System.out.println("홍보 문자 생성 요청이 시작됩니다."); //LOG
            Mono<String> promotiontext = chatGptService.getChatGptResponse(promotionPrompt);
            //LOG
            promotiontext.doOnError(e -> {
                System.out.println("홍보 문자 생성 중 오류 발생: " + e.getMessage());
            }).subscribe(result -> {
                System.out.println("홍보 문자가 생성되었습니다: " + result); // LOG
            });
            System.out.println("홍보 문자 생성 요청이 보내졌습니다.");

            // 비동기 작업 수행
            performAsyncTask(translationPrompt,atmosphere);

            System.out.println("비동기 작업 요청 직후 SecurityContext: " + SecurityContextHolder.getContext());

            return promotiontext;
        } catch (Exception e) {
            System.out.println("예외 발생:" + e.getMessage());
            return null;
        }
    }

    @Async
    public Mono<Void> performAsyncTask(String translationPrompt,String atmosphere) {
        // 비동기 작업 수행
        String text = "";
        try {
            System.out.println("홍보 대상 키워드 번역 요청이 시작됩니다.");
            System.out.println("getChatGptResponse 호출 전에 로그를 남깁니다. 번역 프롬프트: " + translationPrompt);
            chatGptService.getChatGptResponse(translationPrompt)
                            .subscribe(t -> {


            System.out.println("getChatGptResponse 호출 완료. 번역된 텍스트: " + t);


        if (t != null) {
            translatedtext = t.replace("'", "");
        }

        System.out.println(translatedtext); // LOG
        System.out.println("홍보 대상 키워드 번역 요청이 완료되고 번역된 문자가 출력되었습니다."); //LOG
        // 이미지 프롬프트 생성
        String ImagePrompt = chatGptService.makeImagePrompt(translatedtext,atmosphere);
        System.out.println(ImagePrompt);
        System.out.println("이미지 프롬프트를 Dall-E로 보내어 이미지URL 생성을 시작합니다.");
        Mono<String[]> ImageUrl = imageGenerationService.generateImage(ImagePrompt); // 이미지url 3개 받아오기

        ImageUrl.map(imageUrls -> {
            int i = 0;
            for (String url : imageUrls) { // LOG
                System.out.println("이미지URL " + i + "번: " + url);
                i++;
            }
            System.out.println("이미지 URL을 DB에 저장을 시작합니다.");
            imageService.saveImageUrls(imageUrls).subscribe();
            System.out.println("모든 요청과 작업이 완료되었습니다."); //LOG
            return Mono.empty();
        }).subscribe(); });
              } catch (Exception e) {
                  System.out.println(e.getMessage());
        }
        return null;
    }

}
