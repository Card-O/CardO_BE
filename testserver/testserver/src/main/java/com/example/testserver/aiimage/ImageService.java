package com.example.testserver.aiimage;

import com.example.testserver.DB.ImageRepository;
import com.example.testserver.DB.ImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

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

        public String findUrlById(long id) {
            Optional<ImageEntity> imageEntity = imageRepository.findById(id);
            String url = imageEntity.isPresent() ? imageEntity.get().getImageUrl() : null;
            return url;
        }

        public void deleteallimages() {
            imageRepository.deleteAll();
            imageRepository.resetAutoIncrement();
        }
    }
