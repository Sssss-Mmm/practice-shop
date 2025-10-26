package com.example.practice_shop.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


/**
 * 인증 관련 컨트롤러
 * (예: 로그인, 회원가입)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    @PostMapping("/register")
    public String register(@Valid@RequestBody SignupRequest signupRequest) {
        // 회원가입 로직 구현 (예: userService.register(user))
        SignupRequest request = signupRequest;
        userService.register(request);
        return "User registered successfully";
    
    
}
}
