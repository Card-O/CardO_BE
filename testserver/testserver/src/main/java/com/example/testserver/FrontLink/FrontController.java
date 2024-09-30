package com.example.testserver.FrontLink;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FrontController {

    @GetMapping("/data")
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok("Hello from the backend!");
    }
}