package com.example.testserver.DB;



import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.awt.*;

@Table("image_entity")
public class ImageEntity {

    @Id

    private Long id;

    private String image_url;

    public ImageEntity() {
    }
    public ImageEntity(String imageUrl) {
        this.image_url = imageUrl;
    }

    public Long getid() {
        return id;
    }

    public void setid(Long id) {
        this.id = id;
    }

    public String getimage_url() {
        return image_url;
    }

    public void setimage_url(String imageUrl) {
        this.image_url = imageUrl;
    }
}
