package com.example.testserver.DB;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table("image_entity")
public class ImageEntity {

    @Id
    private Integer imagenumber;

    private Long userid;
    private String imageurl;

    public ImageEntity(Long userid, String imageurl) {
        this.userid = userid;
        this.imageurl = imageurl;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Integer getImagenumber() {
        return imagenumber;
    }

    public void setImagenumber(Integer imagenumber) {
        this.imagenumber = imagenumber;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
