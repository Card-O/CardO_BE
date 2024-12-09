package com.example.testserver.DB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AddressBookService {
    private final AddressBookRepository addressBookRepository;

    @Autowired
    public AddressBookService(AddressBookRepository addressBookRepository) {
        this.addressBookRepository = addressBookRepository;
    }

    public Flux<AddressBook> getAddressBooksByUserId(Long userId) {
        return addressBookRepository.findByUserId(userId)
                .switchIfEmpty(Flux.error(new RuntimeException("No address book entries found")));
    }
}
