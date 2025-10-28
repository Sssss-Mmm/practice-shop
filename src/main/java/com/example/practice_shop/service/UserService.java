package com.example.practice_shop.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

import com.example.practice_shop.constant.Role;
import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
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
    /**
     * 로그아웃
     * @param userLogin
     * @return
     */
    public Map<String,String> Login(UserLogin userLogin) {
        String provider = userLogin.getProvider();

        if("local".equalsIgnoreCase(provider)){
            return localLogin(userLogin);
        }
        else {
            return oauthLogin(userLogin);
        }
        
        
    }
    public Map<String,String> localLogin(UserLogin userLogin){
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

    public Map<String,String> oauthLogin(UserLogin userLogin){
        Optional<User> optionalUser = userRepository.findByEmail(userLogin.getEmail());
        User user;

        if(optionalUser.isEmpty()){
            user = User.builder()
            .email(userLogin.getEmail())
            .provider(userLogin.getProvider())
            .providerId(userLogin.getProviderId())
            .name("OAuthUser") // 기본 이름 세팅
            .nickname("user_" + UUID.randomUUID())
            .region("서울")
            .address("기본주소")
            .phoneNumber("010-0000-0000")
            .gender("UNKNOWN")
            .birthDate("1900-01-01")
            .role(Role.USER)
            .build();
        }
        else {
            user = optionalUser.get();
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

    /**
     * 로그아웃
     * @param userLogout
     */
    public void logout(UserLogout userLogout){

        String refreshToken = userLogout.getRefreshToken();

        if(!jwtTokenProvider.validateToken(userLogout.getAccessToken())){
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }
        if(refreshToken == null || !jwtTokenProvider.validateToken(userLogout.getRefreshToken())){
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        String email = jwtTokenProvider.getEmail(refreshToken);

        User user = userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        user.setRefreshToken(null);

        userRepository.save(user);
    }
}
