package com.example.practice_shop.dtos.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {

    private String nickname;
    private String phoneNumber;
    private String region;
    private String address;
    private String gender;
    private String birthDate;
}
