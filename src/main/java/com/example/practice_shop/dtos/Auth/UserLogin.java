package com.example.practice_shop.dtos.Auth;


import jakarta.validation.constraints.Email;
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
    @jakarta.validation.constraints.NotBlank(message = "이메일은 필수 입니다.")
    private String email;
    
    @jakarta.validation.constraints.NotBlank(message = "비밀번호는 필수 입니다.")
    private String password;

    private String provider = "local";

    private String providerId;
}
