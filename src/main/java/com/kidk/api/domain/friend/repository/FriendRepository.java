package com.kidk.api.domain.friend.repository;

import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    List<Friend> findByFriendUser(User user);

    boolean existsByUserAndFriendUser(User user, User friendUser);

    // 상태별 조회
    List<Friend> findByUserAndStatus(User user, FriendStatus status);

    List<Friend> findByFriendUserAndStatus(User user, FriendStatus status);

    // 특정 관계와 상태 조회
    Optional<Friend> findByUserAndFriendUserAndStatus(User user, User friendUser, FriendStatus status);

    // 양방향 친구 관계 확인
    Optional<Friend> findByUserAndFriendUser(User user, User friendUser);
}
