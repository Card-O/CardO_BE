package com.example.testserver.DB;

import reactor.core.publisher.Mono;

public interface CustomImageRepository {
    Mono<Void> resetAutoIncrement();
}

