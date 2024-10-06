package com.example.testserver.chatgpt;
import com.example.testserver.aiimage.ImageGenerationService;
import com.example.testserver.aiimage.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

import java.util.Map;


@RestController
@RequestMapping("/chat")
public class ChatgptController {

    private String ImagePrompt;
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
    public String chat(@RequestBody Map<String, String> data) {
        System.out.println("Chat 메서드 호출됨");
        String when = data.get("input1"); // 홍보 날짜
        String where = data.get("input2"); // 홍보 위치
        String what = data.get("input3"); // 홍보 대상
        String how = data.get("input4"); // 홍보 물체의 무엇을 홍보하는가

        // 첫 번째 요청: 홍보 문자 생성
        String promotionPrompt = chatGptService.makePromotionPrompt(when,where,what,how);

        // 두 번째 요청: 단어 번역
        String translationPrompt = chatGptService.makeTranslationPrompt(what);



        try {       // 홍보 문자 생성
            String promotiontext = chatGptService.getChatGptResponse(promotionPrompt);

            CompletableFuture.runAsync(() -> { // 동시에 코드 실행. 비동기적 흐름
                String text = "";
                try {
                    text = chatGptService.getChatGptResponse(translationPrompt);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

                String translatedtext = text.replace("'",""); //  따옴표 제거
                System.out.println(translatedtext); // LOG
                // ImagePrompt
                String ImagePrompt = chatGptService.makeImagePrompt(translatedtext);
                String[] ImageUrl = imageGenerationService.generateImage(ImagePrompt); // 이미지url 3개 받아오기

                for(String url:ImageUrl){ // LOG
                System.out.println(url);
                }

                imageService.saveImageUrls(ImageUrl);
                System.out.println("ALL SYSTEMS GOING RIGHT!"); //LOG

            });

            return promotiontext;
        } catch (Exception e) {
            return e.getMessage();
        }

    }

}