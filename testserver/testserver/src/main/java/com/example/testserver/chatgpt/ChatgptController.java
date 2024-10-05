package com.example.testserver.chatgpt;
import com.example.testserver.aiimage.ImageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatgptController {

    @Autowired
    private ImageController imageController;

    private final ChatgptService chatGptService;

    public ChatgptController(ChatgptService chatGptService) {
        this.chatGptService = chatGptService;
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
        String promotionRequest = when + ", " + where + ", " + what + ", " + how
                + " 내용의 홍보 문자를 만들어 주세요.";

        // 두 번째 요청: 단어 번역
        String translationRequest = "단어 '" + what + "'를 영어로 번역해 주세요. 결과는 단어만 제공해 주세요.";

        try {       // 홍보 문자 생성
            String promotiontext = chatGptService.getChatGptResponse(promotionRequest);

            CompletableFuture.runAsync(() -> { // 동시에 코드 실행. 비동기적 흐름
                String text = "";
                try {
                    text = chatGptService.getChatGptResponse(translationRequest);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

                String translatedtext = text.replace("'",""); //  따옴표 제거
                System.out.println(translatedtext); // LOG
                // ImagePrompt
                String ImagePrompt = "Design a Card Design featuring " + translatedtext + "without any text or numbers.";
                String[] ImageUrl = imageController.generateImageUrl(ImagePrompt); // 이미지url 3개 받아오기

                for(String url:ImageUrl){ // LOG
                System.out.println(url);
                }

                imageController.ImageURLSave(ImageUrl);
                System.out.println("ALL SYSTEMS GOING RIGHT!"); //LOG

            });

            return promotiontext;
        } catch (Exception e) {
            return e.getMessage();
        }



    }

}