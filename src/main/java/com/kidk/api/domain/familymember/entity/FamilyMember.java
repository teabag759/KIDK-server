package com.kidk.api.domain.familymember.entity;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 가족(families)에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    // 가족 구성원(실제 user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // PARENT, CHILD
    @Column(nullable = false, length = 20)
    private String role;

    // 주 보호자인지 여부
    @Column(name = "is_primary_parent", nullable = false)
    private boolean primaryParent;

    // 누가 초대했는지 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    // PENDING, ACCEPTED, REJECTED
    @Column(nullable = false, length = 20)
    private String status;
}