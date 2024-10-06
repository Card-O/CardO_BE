package com.example.testserver.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    // 가장 작은 ID를 가진 이미지 URL을 반환하는 쿼리
    @Query("SELECT i.imageUrl FROM ImageEntity i WHERE i.id = (SELECT MIN(i2.id) FROM ImageEntity i2)")
    String findSmallestIdImageUrl();



    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE image_entity AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}
