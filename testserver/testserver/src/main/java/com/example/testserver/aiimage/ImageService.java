package com.example.testserver.aiimage;

import com.example.testserver.DB.*;
import com.example.testserver.secure.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;


@Service
public class ImageService {
        private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
        String username = UserContextHolder.getUserId();

        private final ImageRepository imageRepository;
        private final UserRepository userRepository;
        @Autowired
        public ImageService(ImageRepository imageRepository, UserRepository userRepository) {
            this.imageRepository = imageRepository;
            this.userRepository = userRepository;
        }
        @Transactional
        public Mono<Void> saveImageUrls(String[] urls, SecurityContext securityContext) {
            Authentication authentication = securityContext.getAuthentication();
            String username = authentication.getName();
            System.out.println("username:"+username);
            return userRepository.findByUsername(username)
                    .flatMap(users -> { Long user_id = users.getId();
                        System.out.println("UserID:" + user_id);

                        // 각 URL에 대해 Mono<ImageEntity>를 생성하여 저장
                        return Flux.fromArray(urls) // 배열을 Flux로 변환
                                .flatMap(url -> {
                                    System.out.println("Saving url:" + url);
                                    ImageEntity image = new ImageEntity(user_id, url);
                                    System.out.println("이미지 저장 시도");
                                    return imageRepository.saveImage(image)
                                            .doOnError(e -> {logger.error("Error saving image: ", e);}); // 추가된 오류 처리
                                })
                                .then(); // 모든 저장이 완료된 후에 빈 Mono<Void> 반환
                    }); }

    public Mono<String> findNextUrlById(Integer imgnum) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    String username = authentication.getName();
                    return userRepository.findByUsername(username)
                            .flatMap(user -> {
                                Long user_id = user.getId();
                                System.out.println("security context 존재함");
                                return imageRepository.findByUserIdAndImageNumber(user_id, imgnum)
                                        .map(ImageEntity::getImageurl);
                            });

                });
    }
        public void deleteallimages() {
            userRepository.findByUsername(username)
                    .flatMap(user -> {
                        Long userId = user.getId(); // 사용자 ID 가져오기
                        return imageRepository.deleteByUserId(userId); // 이미지 삭제// 오토 인크리먼트 리셋
                    })
                    .subscribe();
        }

    public Mono<String> findLowestImgUrlByUserId(SecurityContext securityContext) {
        // securityContext를 매개변수로 받아와서 사용합니다.
        Authentication authentication = securityContext.getAuthentication();
        String username = authentication.getName(); // 사용자 이름 가져오기

        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    Long userId = user.getId();
                    return imageRepository.findLowestImageUrlByUserId(userId);
                });
    }
    public Mono<Integer> findLowestImgNumByUserId(SecurityContext securityContext) {
        // securityContext를 매개변수로 받아와서 사용합니다.
        System.out.println("ImageService securityContext:" + securityContext);

        Authentication authentication = securityContext.getAuthentication();
        String username = authentication.getName(); // 사용자 이름 가져오기

        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    Long userId = user.getId();
                    return imageRepository.findLowestImageNumByUserId(userId);
                });
    }
    }
