package com.kidk.api.domain.friend.entity;

import com.kidk.api.common.BaseTimeEntity;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends", uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_friend", columnNames = { "user_id", "friend_user_id" })
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

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private FriendStatus status;

        /**
         * 특정 사용자가 이 친구 관계에 포함되는지 확인
         */
        public boolean involvesUser(Long userId) {
                return user.getId().equals(userId) || friendUser.getId().equals(userId);
        }

        /**
         * 특정 사용자가 친구 요청을 받은 사람인지 확인
         */
        public boolean isReceiverUser(Long userId) {
                return friendUser.getId().equals(userId);
        }
}
