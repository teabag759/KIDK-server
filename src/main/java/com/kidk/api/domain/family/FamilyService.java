package com.kidk.api.domain.family;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;

    // 가족 전체 목록 조회 (테스트용)
    public List<Family> findAll() {
        return familyRepository.findAll();
    }

    // 특정 가족 단일 조회
    public Family findById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family Not Found")) ;
    }
}
