package com.kidk.api.domain.savingsgoal;

import com.kidk.api.domain.common.BaseTimeEntity;
import com.kidk.api.domain.common.Status;
import com.kidk.api.domain.mission.Mission;
import com.kidk.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "savings_goals",
        indexes = {
                @Index(name = "idx_savings_goals_user_id", columnList = "user_id"),
                @Index(name = "idx_savings_goals_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.IN_PROGRESS;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

}
