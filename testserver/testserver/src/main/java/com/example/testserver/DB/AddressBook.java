package com.example.testserver.DB;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("address_book")
public class AddressBook {

    @Id
    private Long addressId; // 주소록 아이디
    private Long userId; // 외래키로 사용할 회원 아이디
    private String name; // 이름
    private String phoneNumber; // 전화번호

    public AddressBook() {}

    // Getters 및 Setters
    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}