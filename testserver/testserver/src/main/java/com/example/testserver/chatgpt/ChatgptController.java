package com.example.testserver.chatgpt;
import com.example.testserver.aiimage.ImageGenerationService;
import com.example.testserver.aiimage.ImageService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import java.util.Map;


@RestController
@RequestMapping("/chat")
public class ChatgptController {
    private String translatedtext;
    private final ChatgptService chatGptService;
    private final ImageGenerationService imageGenerationService;
    private final ImageService imageService;

    public ChatgptController(ChatgptService chatGptService,ImageGenerationService imageGenerationService,ImageService imageService) {
        this.chatGptService = chatGptService;
        this.imageGenerationService = imageGenerationService;
        this.imageService = imageService;
    }

    @GetMapping
    public String getchat(){
        return "chat";
    }

    @PostMapping
    public Mono<String> chat(@RequestBody Map<String, String> data) {
        System.out.println("프론트로부터 POST요청을 받고 매핑된 메서드가 실행되었습니다.");
        String when = data.get("input1"); // 홍보 날짜
        String where = data.get("input2"); // 홍보 위치
        String what = data.get("input3"); // 홍보 대상
        String how = data.get("input4"); // 홍보 물체의 무엇을 홍보하는가

        // 첫 번째 요청: 홍보 문자 생성
        String promotionPrompt = chatGptService.makePromotionPrompt(when,where,what,how);
        // 두 번째 요청: 단어 번역
        String translationPrompt = chatGptService.makeTranslationPrompt(what);



        try {       // 홍보 문자 생성
            System.out.println("홍보 문자 생성 요청이 시작됩니다."); //LOG
            Mono<String> promotiontext = chatGptService.getChatGptResponse(promotionPrompt);
            //LOG
            System.out.println("홍보 문자 생성 요청이 보내졌습니다.");
            System.out.println("홍보 문자는 만들어지면 웹상에 띄워지게됩니다.");
            System.out.println("키워드 번역 & 이미지 URL 생성 및 저장은 비동기식으로 실행됩니다.");



            CompletableFuture.runAsync(() -> { // 동시에 코드 실행. 비동기적 흐름
                String text = "";
                try {
                    System.out.println("홍보 대상 키워드 번역 요청이 시작됩니다.");
                    text = chatGptService.getChatGptResponse(translationPrompt).block();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

                if (text != null) {
                    translatedtext = text.replace("'","");
                }

                System.out.println(translatedtext); // LOG
                System.out.println("홍보 대상 키워드 번역 요청이 완료되고 번역된 문자가 출력되었습니다."); //LOG
                // ImagePrompt
                String ImagePrompt = chatGptService.makeImagePrompt(translatedtext);

                System.out.println("이미지 프롬프트를 Dall-E로 보내어 이미지URL 생성을 시작합니다.");
                Mono<String[]> ImageUrl = imageGenerationService.generateImage(ImagePrompt); // 이미지url 3개 받아오기

                ImageUrl.map(imageUrls -> {
                    int i = 0;
                    for (String url : imageUrls) { // LOG
                        System.out.println("이미지URL "+i+"번: "+url);
                        i++;
                    }
                    System.out.println("이미지 URL을 DB에 저장을 시작합니다.");
                    imageService.saveImageUrls(imageUrls);
                    System.out.println("모든 요청과 작업이 완료되었습니다."); //LOG
                    return Mono.empty();
                }).subscribe();
            });

            return promotiontext;
        } catch (Exception e) {
            System.out.println("예외 발생:"+e.getMessage());
            return null;
        }

    }

}