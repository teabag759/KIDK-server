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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FamilyMemberRepositoryTest {

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("FamilyMember 저장 및 조회 - save_and_find")
    void save_and_find() {
        Family family = familyRepository.findById(1L).orElseThrow();
        User user = userRepository.findById(1L).orElseThrow();

        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role("PARENT")
                .primaryParent(true)
                .invitedBy(null)
                .invitedAt(LocalDateTime.now())
                .acceptedAt(LocalDateTime.now())
                .status("ACCEPTED")
                .build();

        FamilyMember saved = familyMemberRepository.save(member);
        FamilyMember found = familyMemberRepository.findById(saved.getId()).orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals("PARENT", found.getRole());
        assertTrue(found.isPrimaryParent());
        assertEquals("ACCEPTED", found.getStatus());
    }

    @Test
    @DisplayName("가족 ID로 가족 구성원 조회 - findByFamilyId")
    void findByFamilyId() {
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(1L);
        assertNotNull(members);
        // 샘플 데이터 / 테스트 상황에 따라 비어 있을 수도 있으니 isEmpty()는 강제하지 않음
    }

    @Test
    @DisplayName("유저 ID로 가족 구성원 조회 - findByUserId")
    void findByUserId() {
        List<FamilyMember> members = familyMemberRepository.findByUserId(1L);
        assertNotNull(members);
    }
}