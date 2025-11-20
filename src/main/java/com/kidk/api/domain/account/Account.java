package com.kidk.api.domain.account;

import com.kidk.api.domain.common.BaseTimeEntity;
import com.kidk.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_primary")
    private Boolean primary;  // null 가능
}
