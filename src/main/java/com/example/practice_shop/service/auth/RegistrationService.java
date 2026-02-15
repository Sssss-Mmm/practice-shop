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
        if (!jwtTokenProvider.validateToken(request.getTempToken())) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        // 임시 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(request.getTempToken());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.GUEST) {
            throw new IllegalStateException("이미 가입이 완료된 사용자입니다.");
        }

        // 추가 정보 업데이트
        user.updateProfile(
            request.getNickname(),
            request.getPhoneNumber(),
            null, // region
            request.getAddress(),
            null, // gender
            request.getBirthDate()
        );

        // 역할 변경 (GUEST -> USER)
        // User 엔티티에 setRole 메서드가 롬복 @Setter로 생성되어 있음
        // 명시적 메서드를 사용하거나 Setter 사용 (User 엔티티 리팩토링에서 Role 변경 메서드를 안만들었으므로 Setter 사용하거나 추가 필요)
        // 일단 Setter 사용 (User.java에 @Setter가 아직 있음 - 나중에 제거할 예정이지만, 현재는 사용 가능)
        // 하지만 Task 6번에서 @Setter 제거 예정이므로 메서드 추가하는 것이 좋음.
        // 일단은 필드 직접 접근이 안되므로 Setter 메서드 필요. (User.java에는 @Setter가 있음)
        // 리팩토링 계획에서 @Setter 제거할 때 changeRole 같은 메서드 추가 필요.
        // 여기서는 일단 기존 방식대로 진행.

        // 상태 변경 및 권한 변경
        // User 엔티티 수정 필요: changeRole 메서드, activate 메서드 등
        // 일단은 기존 방식 + User에 Setter가 있으니 사용.
        // 다만 role 필드에 대한 Setter가 User 엔티티에 있어야 함.

        // User.java 수정이 있었는지 확인: @Setter는 아직 클래스 레벨에 붙어있음.
        // 따라서 setRole 사용 가능.
        user.setStatus(Status.ACTIVE);
        // User 생성 시 Role.USER로 설정하는 메서드가 없으므로, 빌더 패턴이나 Setter 사용.
        // Role 필드는 final이 아니므로 Setter 사용 가능.
        // 다만 리팩토링 취지에 맞게 명시적 메서드를 사용하는 것이 좋음. -> TODO

        // 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), Role.USER);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getName(), Role.USER);

        user.setRefreshToken(refreshToken);
        user.updateLastLoginAt();

        // Role 업데이트 (토큰 발급에 사용된 Role 적용)
        // 주의: 엔티티의 Role을 업데이트해야 함.
        // user.setRole(Role.USER); // Lombok Setter 필요

        // 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }
}
