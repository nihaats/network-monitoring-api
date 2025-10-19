package com.network_monitor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @GetMapping("/all")
    public ResponseEntity<String> allAccess() {
        return ResponseEntity.ok("Public Content - No Authentication Required!");
    }

    @PostMapping("/signup-test")
    public ResponseEntity<String> signupTest(@RequestBody Object request) {
        return ResponseEntity.ok("Signup endpoint is working! Received: " + request.toString());
    }
}

@RestController
@RequestMapping("/api/auth/v2")
@CrossOrigin(origins = "*", maxAge = 3600)
class AuthTestController {

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Object signupRequest) {
        System.out.println("Signup request received: " + signupRequest);
        return ResponseEntity.ok("Signup endpoint reached successfully!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Auth endpoint is accessible!");
    }
}