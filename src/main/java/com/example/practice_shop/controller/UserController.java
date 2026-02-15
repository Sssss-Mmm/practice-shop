package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.User.UserProfileResponse;
import com.example.practice_shop.dtos.User.UserProfileUpdateRequest;
import com.example.practice_shop.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse userProfile = userProfileService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(Authentication authentication, @RequestBody UserProfileUpdateRequest request) {
        String email = authentication.getName();
        UserProfileResponse updatedProfile = userProfileService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }
}
