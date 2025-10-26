package com.example.practice_shop.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.entity.User;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입 처리
     * @param signupRequest
     */
    public void register(SignupRequest signupRequest) {

        User existingUser = userRepository.findByEmail(signupRequest.getEmail()).orElse(null);
        if(existingUser != null) {
            throw new IllegalArgumentException("Email already in use"); // IllegalArgumentException 예외 발생
        }
        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword() != null ? passwordEncoder.encode(signupRequest.getPassword()) : null)
                .name(signupRequest.getName())
                .phoneNumber(signupRequest.getPhoneNumber())
                .nickname(signupRequest.getNickname())
                .region(signupRequest.getRegion())
                .address(signupRequest.getAddress())
                .gender(signupRequest.getGender())
                .birthDate(signupRequest.getBirthDate())
                .role(signupRequest.getRole())
                .provider(signupRequest.getProvider())
                .providerId(signupRequest.getProviderId() != null ? signupRequest.getProviderId() : "local_"+ UUID.randomUUID())
                .status(Status.ACTIVE)
                .build();

        

        userRepository.save(user);
    }

    public Map<String,String> Login(UserLogin userLogin) {
        
        // 이메일 검증
        User user = userRepository.findByEmail(userLogin.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    
        // 비밀번호 검증
        if(!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치 하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createAccessToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        //반환 데이터 구성
        Map<String,String> response = new HashMap<>();
        response.put("accessToken",accessToken);
        response.put("refreshToken",refreshToken);
        
        return response;
    }
}
