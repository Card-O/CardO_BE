package com.example.testserver.secure;

import com.example.testserver.DB.UserRepository;
import com.example.testserver.DB.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 저장할 리포지토리

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .doOnNext(user -> {
                    System.out.println("Found user: " + user.getUsername());
                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .doOnNext(user -> {
                    // User 객체 확인 로그
                    System.out.println("Returning user: " + user);
                })
                .map(user -> {
                    // UserDetails 객체 생성
                    org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            user.getPassword(),
                            new ArrayList<>()
                    );

                    // UserDetails 로그 출력
                    System.out.println("UserDetails created: " + userDetails);

                    return userDetails; // UserDetails 반환
                });
    }


}
