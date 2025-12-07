package com.kidk.api.domain.mission.entity;

import com.kidk.api.domain.user.entity.User;
import com.kidk.api.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 미션 생성자 (주로 부모)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // 미션 수행자 (주로 아이)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "mission_type", nullable = false, length = 20)
    private String missionType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "reward_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal rewardAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
}
