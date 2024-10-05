package com.example.testserver.aiimage;

import com.example.testserver.DB.ImageRepository;
import com.example.testserver.DB.ImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Service
public class ImageService {

        @Autowired
        private ImageRepository imageRepository;

        @Transactional
        public void saveImageUrls(String[] urls) {
            for (String url : urls) {
                System.out.println("Saving url:" + url);
                ImageEntity image = new ImageEntity(url);
                imageRepository.save(image);
            }
        }

        public String getSmallestIDurl() {
            return imageRepository.findSmallestIdImageUrl();
        }
    }
