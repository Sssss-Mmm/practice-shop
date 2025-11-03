package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Auth.ForgotPasswordRequest;
import com.example.practice_shop.dtos.Auth.OAuth2RegistrationRequest;
import com.example.practice_shop.dtos.Auth.ResetPasswordRequest;
import com.example.practice_shop.dtos.Auth.ResendVerificationEmailRequest;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
import com.example.practice_shop.dtos.Auth.VerifyEmailRequest;
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
     * oauth2 사용자 정보 없는 경우 회원정보 등록 엔드포인트입니다.
     * @param signupRequest 회원가입 정보
     * @return 성공 메시지
     */
    @PostMapping("/oauth2/register")
    @Operation(summary = "OAuth2 회원가입 완료",description = "OAuth2로 인증된 사용자가 추가 정보를 입력하여 회원가입을 완료합니다.")
    public ResponseEntity<Map<String, String>> oauth2CompleteRegistration(@Valid @RequestBody OAuth2RegistrationRequest request){
        Map<String, String> tokens = userService.completeOAuth2Registration(request);
        return ResponseEntity.ok(tokens);
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
    /**
     * refresh token 재발급 엔드포인트입니다.
     * @param refreshToken
     * @return 새로운 Access Token과 Refresh Token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "토큰 재발급",description = "사용자가 만료된 Access Token을 갱신합니다.")
    public ResponseEntity<Map<String,String>> refreshToken(@RequestBody String refreshToken){
        Map<String,String> response = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰을 검증합니다.")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request.getToken());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/verify-email/resend")
    @Operation(summary = "이메일 인증 재발송", description = "인증 이메일을 다시 발송합니다.")
    public ResponseEntity<String> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        userService.resendEmailVerification(request.getEmail());
        return ResponseEntity.ok("인증 이메일을 다시 발송했습니다.");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 이메일을 발송합니다.")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("비밀번호 재설정 안내 이메일을 발송했습니다.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "토큰을 검증하고 새 비밀번호로 변경합니다.")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("비밀번호를 재설정했습니다.");
    }
}
