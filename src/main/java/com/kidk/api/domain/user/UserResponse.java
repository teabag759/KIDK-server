package com.kidk.api.domain.user;

import com.kidk.api.domain.common.BaseTimeEntity;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
public class UserResponse {
    private Long id;
    private String firebaseUid;
    private String email;
    private String socialProvider;
    private String socialProviderId;
    private String userType;
    private String name;
    private String profileImageUrl;
    private LocalDate birthDate;
    private String phone;
    private String status;
    private LocalDateTime statusChangedAt;
    private LocalDateTime lastLoginAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.firebaseUid = user.getFirebaseUid();
        this.email = user.getEmail();
        this.socialProvider = user.getSocialProvider();
        this.socialProviderId = user.getSocialProviderId();
        this.userType = user.getUserType();
        this.name = user.getName();
        this.profileImageUrl = user.getProfileImageUrl();
        this.birthDate = user.getBirthDate();
        this.phone = user.getPhone();
        this.status = user.getStatus();
        this.statusChangedAt = user.getStatusChangedAt();
        this.lastLoginAt = user.getLastLoginAt();

    }
}
