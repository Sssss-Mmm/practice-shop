package com.example.practice_shop.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<String> register(@Valid@RequestBody SignupRequest signupRequest) {
        // 회원가입 로직 구현 (예: userService.register(user))
        SignupRequest request = signupRequest;
        userService.register(request);
        return ResponseEntity.ok("회원가입 성공");
}
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자가 로그인을 합니다.")
    public ResponseEntity<Map<String,String>> login(@RequestBody UserLogin userLogin) {
        Map<String,String> response  = userService.Login(userLogin);
        return ResponseEntity.ok(response);
    }
}
