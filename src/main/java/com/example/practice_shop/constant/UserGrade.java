package com.example.practice_shop.constant;

public enum UserGrade{
    BASIC("기본 회원", 0),
    SILVER("실버 회원", 300_000),
    GOLD("골드 회원", 1_000_000),
    PLATINUM("플래티넘 회원", 2_000_000);

    public final String description;
    public final long requiredAmount;

    UserGrade(String description,long requiredAmount) {
        this.description = description;
        this.requiredAmount = requiredAmount;
    }

    public static UserGrade fromTotalSpend(long totalSpend) {
        if (totalSpend >= PLATINUM.requiredAmount) return PLATINUM;
        if (totalSpend >= GOLD.requiredAmount) return GOLD;
        if (totalSpend >= SILVER.requiredAmount) return SILVER;
        return BASIC;
    }
}
