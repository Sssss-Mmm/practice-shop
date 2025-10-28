package com.example.practice_shop.dtos.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLogout {
    String accessToken;
    String refreshToken;
}
