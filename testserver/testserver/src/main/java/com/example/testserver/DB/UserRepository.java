package com.example.testserver.DB;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User,Long> {
    Mono<User> findByUsername(String username);
    // 필요한 추가 쿼리 메서드를 정의할 수 있습니다.
}
