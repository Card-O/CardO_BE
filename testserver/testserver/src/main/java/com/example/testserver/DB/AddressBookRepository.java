package com.example.testserver.DB;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AddressBookRepository extends R2dbcRepository<AddressBook, Long> {

    @Query("SELECT a.* FROM address_book a JOIN user u ON a.userid = u.id WHERE u.id = :userId")
    Flux<AddressBook> findByUserId(Long userId);
}