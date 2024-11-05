package com.example.testserver.DB;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Mono;

@Repository
public class ImagePromptRepository {

   private final R2dbcEntityTemplate r2dbcEntityTemplate;

   public ImagePromptRepository(R2dbcEntityTemplate r2dbcEntityTemplate) {
       this.r2dbcEntityTemplate = r2dbcEntityTemplate;
   }

    public Mono<String> findImagePromptByUserId(Long userId) {
        return r2dbcEntityTemplate.select(ImagePrompt.class)
                .matching(Query.query(Criteria.where("userid").is(userId)))
                .first()
                .map(ImagePrompt::getImagePrompt);
    }

    public Mono<ImagePrompt> saveImagePrompt(Long userId, String imagePrompt) {
        ImagePrompt newImagePrompt = new ImagePrompt(userId,imagePrompt);
        return r2dbcEntityTemplate.insert(newImagePrompt);
    }
}
