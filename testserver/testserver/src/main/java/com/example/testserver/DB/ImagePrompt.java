package com.example.testserver.DB;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("image_prompt")
public class ImagePrompt {
    @Id
    private Long ipid;

    private Long userid;

    @Column("imageprompt")
    private String imagePrompt;

    public ImagePrompt(Long userid, String imagePrompt) {
        this.userid = userid;
        this.imagePrompt = imagePrompt;
    }

    public Long getIpid() {
        return ipid;
    }

    public void setIpid(Long ipid) {
        this.ipid = ipid;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getImagePrompt() {
        return imagePrompt;
    }

    public void setImagePrompt(String imagePrompt) {
        this.imagePrompt = imagePrompt;
    }
}
