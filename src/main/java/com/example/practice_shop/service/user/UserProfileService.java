package com.example.practice_shop.service.user;

import com.example.practice_shop.dtos.User.UserProfileResponse;
import com.example.practice_shop.dtos.User.UserProfileUpdate;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.exception.ErrorCode;
import com.example.practice_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 정보를 조회합니다.
     * @param email 사용자 이메일
     * @return 사용자 프로필 DTO
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.from(user);
    }

    /**
     * 사용자 프로필 정보를 수정합니다.
     * @param email 사용자 이메일
     * @param profileUpdate 프로필 수정 요청 DTO
     */
    @Transactional
    public UserProfileResponse updateUserProfile(String email, UserProfileUpdate profileUpdate) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 확인 (본인 닉네임 제외)
        if (!user.getNickname().equals(profileUpdate.getNickname()) &&
                userRepository.existsByNickname(profileUpdate.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 프로필 정보 업데이트
        user.updateProfile(
            profileUpdate.getNickname(),
            profileUpdate.getPhoneNumber(),
            profileUpdate.getRegion(),
            profileUpdate.getAddress(),
            user.getGender(), // 기존 값 유지 (DTO에 필드가 없다면)
            user.getBirthDate() // 기존 값 유지
        );
        
        // 변경된 정보 반환
        return UserProfileResponse.from(user);
    }
}
