package com.example.practice_shop.dtos.User;

import com.example.practice_shop.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private String email;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String region;
    private String address;
    private String gender;
    private String birthDate;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .region(user.getRegion())
                .address(user.getAddress())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .build();
    }
}
