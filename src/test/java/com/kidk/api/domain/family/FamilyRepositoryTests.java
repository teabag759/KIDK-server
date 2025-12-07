package com.kidk.api.domain.family;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.family.repository.FamilyRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FamilyRepositoryTests {

    @Autowired
    private FamilyRepository familyRepository;

    @Test
    @DisplayName("Family 저장 및 조회")
    void saveAndFind() {
        Family family = Family.builder()
                .familyName("테스트 가족")
                .build();

        Family saved = familyRepository.save(family);

        Family found = familyRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals("테스트 가족", found.getFamilyName());
    }
}
