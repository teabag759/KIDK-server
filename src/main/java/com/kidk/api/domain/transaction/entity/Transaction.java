package com.kidk.api.domain.transaction.entity;

import com.kidk.api.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_account_id", columnList = "account_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK : accounts.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "related_mission_id")
    private Long relatedMissionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
