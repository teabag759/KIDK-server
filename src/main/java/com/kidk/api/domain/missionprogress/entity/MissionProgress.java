package com.kidk.api.domain.missionprogress.entity;

import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: missions.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    // FK: users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 정량적 진행량 (금액 기반 미션)
    @Column(name = "progress_amount", precision = 15, scale = 2)
    private BigDecimal progressAmount;

    // 퍼센트 기반 진행률
    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @PrePersist
    @PreUpdate
    protected void onActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}
