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
public class UserLogin {
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    
    private String password;

    @NotBlank(message = "provider는 필수 입니다.")
    private String provider;

    private String providerId;
}
