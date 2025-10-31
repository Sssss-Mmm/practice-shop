package com.example.practice_shop.dtos.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2RegistrationRequest {

    @NotBlank(message = "임시 토큰은 필수입니다.")
    private String temporaryToken;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Pattern(regexp = "^(010|011|016|017|018|019)[0-9]{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phoneNumber;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;

    @NotBlank(message = "지역은 필수 입력 항목입니다.")
    private String region;

    @NotBlank(message = "상세 주소는 필수 입력 항목입니다.")
    private String address;

    @NotBlank(message = "성별은 필수 입력 항목입니다.")
    private String gender;

    @NotBlank(message = "생년월일은 필수 입력 항목입니다.")
    private String birthDate;
}
