package com.kidk.api.domain.family;

import com.kidk.api.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name="families")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Family extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name", length = 100)
    private String familyName;

    // 초대코드(추가 필드)
    @Column(name = "invite_code", nullable = false, unique = true, length = 50)
    private String inviteCode;

    // onCreate() 오버라이드
    @Override
    protected void onCreate() {
        super.onCreate();
        generateInviteCode();
    }

    public void generateInviteCode() {
        if (this.inviteCode == null) {
            this.inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
