package com.kidk.api.domain.familymember;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    // 전체 조회 (테스트용)
    @GetMapping
    public List<FamilyMember> getAllFamilyMembers() {
        return familyMemberService.findAll();
    }

    // 단일 조회
    @GetMapping("/{id}")
    public FamilyMember getFamilyMember(@PathVariable Long id) {
        return familyMemberService.findById(id);
    }

    // 가족 기준 조회
    @GetMapping("/family/{familyId}")
    public List<FamilyMember> getFamilyMembersByFamily(@PathVariable Long familyId) {
        return familyMemberService.findByFamilyId(familyId);
    }

    // 유저 기준 조회
    @GetMapping("/user/{userId}")
    public List<FamilyMember> getFamilyMembersByUser(@PathVariable Long userId) {
        return familyMemberService.findByUserId(userId);
    }
}