package com.example.practice_shop.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.practice_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.entity.User;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * @param signupRequest
     */
    public void register(SignupRequest signupRequest) {

        User existingUser = userRepository.findByEmail(signupRequest.getEmail());
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
}
