package com.kidk.api.domain.familymember;

import com.kidk.api.domain.family.Family;
import com.kidk.api.domain.family.FamilyRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FamilyMemberRepositoryTest {

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Test
    @DisplayName("FamilyMember 저장 및 조회")
    void save_and_find() {

        // 1) User 생성
        User user = userRepository.save(
                User.builder()
                        .firebaseUid("test-uid")
                        .email("test@example.com")
                        .userType("PARENT")
                        .name("테스트유저")
                        .status("ACTIVE")
                        .build()
        );

        // 2) Family 생성
        Family family = familyRepository.save(
                Family.builder()
                        .familyName("테스트 가족")
                        .build()
        );

        // 3) FamilyMember 생성
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role("PARENT")
                .primaryParent(true)
                .status("ACCEPTED")
                .build();

        FamilyMember saved = familyMemberRepository.save(member);

        FamilyMember found = familyMemberRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals("PARENT", found.getRole());
    }
}
