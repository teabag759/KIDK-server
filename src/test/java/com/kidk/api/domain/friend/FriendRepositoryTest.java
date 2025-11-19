package com.kidk.api.domain.friend;

import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FriendRepositoryTest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("친구 추가 성공~!")
    void testAddFriend() {
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();

        Friend friend = Friend.builder()
                .user(user1)
                .friendUser(user2)
                .status("ACCEPTED")
                .build();

        Friend saved = friendRepository.save(friend);

        System.out.println("Saved friend id = " + saved.getId());
    }

}
