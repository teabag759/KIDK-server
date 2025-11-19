package com.kidk.api.domain.familymember;

import com.kidk.api.domain.family.Family;
import com.kidk.api.domain.family.FamilyRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    // 전체 조회 (테스트용)
    public List<FamilyMember> findAll() {
        return familyMemberRepository.findAll();
    }

    // ID로 단일 조회
    public FamilyMember findById(Long id) {
        return familyMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FamilyMember not found"));
    }

    // 특정 가족의 구성원 목록
    public List<FamilyMember> findByFamilyId(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        return familyMemberRepository.findByFamily(family);
    }

    // 특정 유저가 속한 가족 정보(들) 조회
    public List<FamilyMember> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return familyMemberRepository.findByUser(user);
    }
}