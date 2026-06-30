package com.example.practice_shop.constant;

public enum Role {
    GUEST("소셜 가입 미완료 사용자"),
    USER("일반 사용자"),
    ADMIN("관리자");

    public final String description;
    Role(String description) {this.description = description;}
}
