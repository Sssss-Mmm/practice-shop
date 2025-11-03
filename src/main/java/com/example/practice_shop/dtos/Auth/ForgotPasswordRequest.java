package com.example.practice_shop.dtos.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @Email(message = "올바른 이메일 형식을 입력해 주세요.")
    @NotBlank(message = "이메일은 필수 값입니다.")
    private String email;
}
