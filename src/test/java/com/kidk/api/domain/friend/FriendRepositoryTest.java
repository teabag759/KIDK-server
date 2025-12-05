package com.kidk.api.domain.friend;

import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.repository.FriendRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FriendRepositoryTest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("친구 추가 성공")
    void testAddFriend() {

        // 유저 1 생성
        User user1 = userRepository.save(
                User.builder()
                        .firebaseUid("uid1")
                        .email("user1@example.com")
                        .userType("PARENT")
                        .name("유저1")
                        .status("ACTIVE")
                        .build()
        );

        // 유저 2 생성
        User user2 = userRepository.save(
                User.builder()
                        .firebaseUid("uid2")
                        .email("user2@example.com")
                        .userType("PARENT")
                        .name("유저2")
                        .status("ACTIVE")
                        .build()
        );

        // friend 생성
        Friend friend = Friend.builder()
                .user(user1)
                .friendUser(user2)
                .status("ACCEPTED")
                .build();

        Friend saved = friendRepository.save(friend);

        assertNotNull(saved.getId());
    }
}
