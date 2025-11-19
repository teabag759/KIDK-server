package com.kidk.api.domain.family;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class FamilyRepositoryTest {


    @Autowired
    private FamilyRepository familyRepository;

    @Test
    void save_and_find() {
        Family family = Family.builder()
                .familyName("test")
                .build();

        Family saved = familyRepository.save(family);
        Family found = familyRepository.findById(saved.getId()).orElseThrow();

        assertEquals(saved.getId(), found.getId());
    }

}
