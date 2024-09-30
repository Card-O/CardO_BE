package com.example.testserver.chatgpt;
import com.example.testserver.aiimage.ImageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

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
    public ResponseEntity<PromotionImageResponse> chat(@RequestBody String userMessage) {
        System.out.println("chat 메서드가 호출되었습니다."); // 로그 추가
        String plusString = "그런 다음, 그 문구에 맞는 이미지 생성 프롬프트도 만들어주세요. 홍보 문구와 영어 이미지 프롬프트는 어떠한 추가 설명 없이 두 내용을 '---'로 구분해주세요.";
        String userMessageAdded = userMessage + plusString;
        try {
            String response = chatGptService.getChatGptResponse(userMessageAdded);

            String[] parts = response.split("---");

            String promotionText = parts[0].replace("[홍보 문구]", "").replace("홍보 문구:","").trim();
            String imagePrompt = parts[1].replace("[영어 이미지 프롬프트]", "").trim();
            byte[] image = imageController.generateImage(imagePrompt);

            // 3. 바이트 배열을 Base64 문자열로 변환
            String imageBase64 = Base64.getEncoder().encodeToString(image);
            System.out.println(promotionText);
            System.out.println(imageBase64);
            return ResponseEntity.ok()
                    .body(new PromotionImageResponse(promotionText, imageBase64));
        } catch (Exception e) {
            e.printStackTrace();
            PromotionImageResponse errorResponse = new PromotionImageResponse("에러가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 Internal Server Error
        }
    }

}
class PromotionImageResponse {
    private String promotionText;
    private String imageBase64;

    public PromotionImageResponse(String promotionText, String imageBase64) {
        this.promotionText = promotionText;
        this.imageBase64 = imageBase64;
    }

    // Getter와 Setter
    public String getPromotionText() {
        return promotionText;
    }

    public String getImageBase64() {
        return imageBase64;
    }
}