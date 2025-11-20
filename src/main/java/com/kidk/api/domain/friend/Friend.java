package com.kidk.api.domain.friend;

import com.kidk.api.domain.common.BaseTimeEntity;
import com.kidk.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_friend",
                        columnNames = {"user_id", "friend_user_id"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 요청을 보낸 사람 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 친구 요청을 받은 사람 (Friend User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    @Column(nullable = false, length = 20)
    private String status;  // REQUESTED, ACCEPTED, REJECTED 등
}
