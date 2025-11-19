package com.kidk.api.domain.user;

import com.kidk.api.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_firebase_uid", columnList = "firebase_uid"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_social_provider_id", columnList = "social_provider_id"),
                @Index(name = "idx_users_status", columnList = "status"),
                @Index(name = "idx_users_last_login_at", columnList = "last_login_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 255)
    private String firebaseUid;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "social_provider", length = 50)
    private String socialProvider;       // APPLE, KAKAO, NAVER, EMAIL

    @Column(name = "social_provider_id", length = 255)
    private String socialProviderId;     // 소셜 로그인 제공자의 고유 ID

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;             // CHILD, PARENT

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "profile_image_url")
    private String profileImageUrl;      // Firebase Storage URL

    @Column(name = "birth_date")
    private LocalDate birthDate;         // 어린이 연령 확인용

    @Column(length = 20)
    private String phone;                // 부모 연락처 (선택)

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";    // ACTIVE, DORMANT, WITHDRAWN, DELETED

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}