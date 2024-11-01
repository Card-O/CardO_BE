package com.example.testserver.DB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/address")
public class AddressBookController {
    private final AddressBookService addressBookService;

    @Autowired
    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @GetMapping("/{userId}")
    public Flux<AddressBook> getAddressBooks(@PathVariable Long userId) {
        return addressBookService.getAddressBooksByUserId(userId);
    }
}
