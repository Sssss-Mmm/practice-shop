package com.example.practice_shop.constant;

public enum Status {
    ACTIVE("활성 상태"),
    INACTIVE("비활성 상태"),
    BANNED("차단된 상태"),
    DELETED("삭제된 상태");

    public final String description;
    Status(String description) {
        this.description = description;
    }

}
