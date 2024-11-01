package com.example.testserver.aiimage;

import com.example.testserver.DB.CustomImageRepositoryImpl;
import com.example.testserver.DB.ImageRepository;
import com.example.testserver.DB.ImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class ImageService {


        private final ImageRepository imageRepository;
        @Autowired
        public ImageService(ImageRepository imageRepository) {
            this.imageRepository = imageRepository;
        }
        @Transactional
        public Mono<Void> saveImageUrls(String[] urls) {
            for (String url : urls) {
                System.out.println("Saving url:" + url);
                ImageEntity image = new ImageEntity(url);
                System.out.println("이미지 저장 시도");
                imageRepository.save(image).subscribe();
            }
            return Mono.empty();
        }

    public Mono<String> findUrlById(long id) {
        return imageRepository.findById(id) // Returns Mono<ImageEntity>
                .map(ImageEntity::getimage_url) // Get the image URL from the ImageEntity
                .switchIfEmpty(Mono.just("Image Not found")); // Return null if the entity is not found
    }

        public void deleteallimages() {
            imageRepository.deleteAll().subscribe();
            imageRepository.resetAutoIncrement().subscribe();
        }
    }
