package com.kidk.api.domain.friend.repository;

import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    List<Friend> findByFriendUser(User user);

    boolean existsByUserAndFriendUser(User user, User friendUser);
}
