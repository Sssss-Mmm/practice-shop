package com.example.practice_shop.service.auth;

import com.example.practice_shop.constant.Role;
import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.OAuth2RegistrationRequest;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.exception.ErrorCode;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    /**
     * 로컬 회원 가입을 처리합니다.
     * @param signupRequest 회원 가입 요청 정보 DTO
     * @return 가입된 사용자의 ID
     */
    @Transactional
    public Long register(SignupRequest signupRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .name(signupRequest.getName())
                .phoneNumber(signupRequest.getPhoneNumber())
                .nickname(signupRequest.getNickname())
                .role(Role.USER) // 기본 역할은 USER
                .status(Status.INACTIVE) // 이메일 인증 전까지 비활성 상태
                .emailVerified(false)
                .build();

        // 사용자 상세 정보 설정 (엔티티 메서드 활용)
        user.updateProfile(
            signupRequest.getNickname(),
            signupRequest.getPhoneNumber(),
            null, // region
            signupRequest.getAddress(),
            null, // gender
            null  // birthDate
        );

        // 데이터베이스에 저장
        userRepository.save(user);

        // 이메일 인증 토큰 생성 및 전송
        emailVerificationService.applyEmailVerificationWindow(user);
        emailVerificationService.sendVerificationEmail(user);

        return user.getId();
    }

    /**
     * OAuth2 로그인 후 추가 정보 입력을 통해 회원 가입을 완료합니다.
     * @param request 추가 정보 입력 요청 DTO
     * @return Access Token과 Refresh Token이 담긴 Map
     */
    @Transactional
    public Map<String, String> completeOAuth2Registration(OAuth2RegistrationRequest request) {
        // 임시 토큰 검증
        if (!jwtTokenProvider.validateToken(request.getTemporaryToken())) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        // 임시 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(request.getTemporaryToken());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 가입 완료 여부는 프로필 충족 여부로만 판단한다(OAuth2AuthenticationSuccessHandler와 동일 기준).
        // role은 결과일 뿐 게이트가 아니므로 여기서 검사하지 않는다.

        // 추가 정보 업데이트
        user.setName(request.getName());
        user.updateProfile(
            request.getNickname(),
            request.getPhoneNumber(),
            request.getRegion(),
            request.getAddress(),
            request.getGender(),
            request.getBirthDate()
        );

        // GUEST -> USER 승격 및 계정 활성화
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);

        // 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), Role.USER);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getName(), Role.USER);

        user.setRefreshToken(refreshToken);
        user.updateLastLoginAt();

        // 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }
}
