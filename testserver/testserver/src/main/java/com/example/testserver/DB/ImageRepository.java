package com.example.testserver.DB;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@Repository
public class ImageRepository {
    private final R2dbcEntityTemplate template;

    public ImageRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    public Mono<ImageEntity> findByUserIdAndImageNumber(Long userid, Integer imagenumber) {
        return template.select(ImageEntity.class)
                .matching(Query.query(Criteria.where("userid").is(userid)
                        .and("imagenumber").is(imagenumber)))
                .one();
    }

    public Mono<Void> saveImage(ImageEntity image) {
        return template.insert(ImageEntity.class).using(image).then();
    }

    public Mono<Void> deleteByUserId(Long userid) {
        return template.delete(ImageEntity.class)
                .matching(Query.query(Criteria.where("userid").is(userid)))
                .all()
                .then();
    }

    // 사용자 ID로 가장 낮은 imagenumber의 imageurl을 찾는 메서드
    public Mono<String> findLowestImageUrlByUserId(Long userId) {
        return template.select(ImageEntity.class)
                .matching(Query.query(Criteria.where("userid").is(userId))
                        .sort(Sort.by("imagenumber").ascending()))
                .first()
                .map(ImageEntity::getImageurl);


    }
    public Mono<Integer> findLowestImageNumByUserId(Long userId) {
        return template.select(ImageEntity.class)
                .matching(Query.query(Criteria.where("userid").is(userId))
                        .sort(Sort.by("imagenumber").ascending()))
                .first()
                .map(ImageEntity::getImagenumber);
    }


}
