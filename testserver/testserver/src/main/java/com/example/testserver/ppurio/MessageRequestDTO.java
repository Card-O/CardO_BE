package com.example.testserver.ppurio;

// MessageRequestDTO.java
import java.util.List;

public class MessageRequestDTO {
    private String promotiontext; // 프로모션 텍스트
    private byte[] image;         // 이미지 URL 또는 Base64 데이터
    private String sendNumber;    // 발신번호
    private List<String> receiveNumbers; // 수신번호 목록


    public MessageRequestDTO(String promotiontext, byte[] image, String sendNumber, List<String> receiveNumbers) {
        this.promotiontext = promotiontext;
        this.image = image;
        this.sendNumber = sendNumber;
        this.receiveNumbers = receiveNumbers;
    }

    // Getters and Setters
    public String getPromotiontext() {
        return promotiontext;
    }

    public void setPromotiontext(String promotiontext) {
        this.promotiontext = promotiontext;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getSendNumber() {
        return sendNumber;
    }

    public void setSendNumber(String sendNumber) {
        this.sendNumber = sendNumber;
    }

    public List<String> getReceiveNumbers() {
        return receiveNumbers;
    }

    public void setReceiveNumbers(List<String> receiveNumbers) {
        this.receiveNumbers = receiveNumbers;
    }
}
