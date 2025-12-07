package com.kidk.api.domain.refreshtoken.entity;

import com.kidk.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 기록용
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_token", columnList = "token"),
                @Index(name = "idx_refresh_tokens_device_id", columnList = "device_id")
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "device_id", length = 255)
    private String deviceId; // 기기 식별자

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 90일 후 만료

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // 마지막 사용 시점 (오래된 기기 삭제용)

    @Builder.Default
    @Column(name = "is_valid", nullable = false)
    private boolean isValid = true;

    // 사용 시점 업데이트 메서드
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    // 토큰 교체 메서드 (Rotation)
    public void rotateToken(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
        this.lastUsedAt = LocalDateTime.now();
    }
}