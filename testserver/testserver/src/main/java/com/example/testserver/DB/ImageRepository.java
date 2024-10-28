package com.example.testserver.DB;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends R2dbcRepository<ImageEntity, Long>,CustomImageRepository{

}
