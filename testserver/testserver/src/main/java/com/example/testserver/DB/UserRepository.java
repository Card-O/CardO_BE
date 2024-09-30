package com.example.testserver.DB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    // 필요한 추가 쿼리 메서드를 정의할 수 있습니다.
}
