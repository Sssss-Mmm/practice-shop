package com.example.practice_shop.constant;

public enum Role {
    USER(" 일반 사용자"),
    ADMIN(" 관리자");

    public final String description;
    Role(String description) {this.description = description;}
}
