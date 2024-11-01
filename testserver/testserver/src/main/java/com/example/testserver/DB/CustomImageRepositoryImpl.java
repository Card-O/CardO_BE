package com.example.testserver.DB;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CustomImageRepositoryImpl implements CustomImageRepository {

    private final DatabaseClient databaseClient;

    public CustomImageRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> resetAutoIncrement() {
        return databaseClient.sql("ALTER TABLE image_entity AUTO_INCREMENT = 1")
                .fetch()
                .rowsUpdated()
                .then();
    }
}

