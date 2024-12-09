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
    private final UserRepository userRepository;

    @Autowired
    public AddressBookController(AddressBookService addressBookService, UserRepository userRepository) {
        this.addressBookService = addressBookService;
        this.userRepository = userRepository;
    }

//    @GetMapping("/{userId}")
//    public Flux<AddressBook> getAddressBooks(@PathVariable Long userId) {
//        return addressBookService.getAddressBooksByUserId(userId);
//    }

    @GetMapping("/{username}")
    public Flux<AddressBook> getAddressBooksByUsername(@PathVariable String username) {
        // username으로 User를 찾고, 해당 User의 id를 이용해 주소록 조회
        return userRepository.findByUsername(username)
                .flatMapMany(user -> addressBookService.getAddressBooksByUserId(user.getId()))
                .switchIfEmpty(Flux.error(new RuntimeException("User not found or no address book entries")));
    }

}
