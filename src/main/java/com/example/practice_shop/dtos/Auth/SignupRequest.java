package com.example.practice_shop.dtos.Auth;

import com.example.practice_shop.constant.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
public class SignupRequest {
    /** 기본 정보 **/
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Pattern(regexp = "^(010|011|016|017|018|019)[0-9]{7,8}$", message = "전화번호 형식이 올바르지 않습니다.")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phoneNumber;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;

    /** 주소 관련 **/
    @NotBlank(message = "지역은 필수 입력 항목입니다.")
    private String region;

    @NotBlank(message = "상세 주소는 필수 입력 항목입니다.")
    private String address;

    /** 부가 정보 **/
    @NotBlank(message = "성별은 필수 입력 항목입니다.")
    private String gender;

    @NotBlank(message = "생년월일은 필수 입력 항목입니다.")
    private String birthDate;

    /** 권한 / 로그인 방식 **/
    private Role role = Role.USER;
    private String provider = "local";
    private String providerId;

}
