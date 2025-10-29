package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
import com.example.practice_shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 관련 API 엔드포인트를 제공하는 컨트롤러입니다.
 * (로컬 회원가입, 로컬 로그인, 로그아웃)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    /**
     * 로컬 사용자 회원가입 엔드포인트입니다.
     * @param signupRequest 회원가입 정보
     * @return 성공 메시지
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<String> register(@Valid @RequestBody SignupRequest signupRequest) {
        userService.register(signupRequest);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로컬 사용자 로그인 엔드포인트입니다.
     * @param userLogin 로그인 정보 (이메일, 비밀번호)
     * @return Access Token과 Refresh Token
     */
    @PostMapping("/login")
    @Operation(summary = "로컬 로그인", description = "사용자가 이메일과 비밀번호로 로그인을 합니다.")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLogin userLogin) {
        Map<String,String> response  = userService.localLogin(userLogin);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃 엔드포인트입니다.
     * @param userLogout 로그아웃 요청 정보 (Access Token, Refresh Token)
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃",description = "사용자가 로그아웃을 합니다.")
    public ResponseEntity<String> logout(@RequestBody UserLogout userLogout){
        userService.logout(userLogout);
        return ResponseEntity.ok("로그아웃 성공");
    }
}
